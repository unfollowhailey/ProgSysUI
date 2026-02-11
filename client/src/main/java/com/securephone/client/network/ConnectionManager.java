package com.securephone.client.network;

import com.securephone.client.models.ChatMessage;
import com.securephone.client.models.Contact;
import com.securephone.client.models.UserSession;
import com.securephone.client.utils.Logger;
import com.securephone.shared.protocol.ChatPacket;
import com.securephone.shared.protocol.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionManager {

	public interface AuthListener {
		void onLoginSuccess(UserSession session);
		void onLoginRequires2FA(String message);
		void onLoginFailed(String reason);
		void onRegisterSuccess(String message);
		void onRegisterFailed(String reason);
	}

	public interface ChatListener {
		void onMessage(ChatMessage message);
	}

	public interface ContactListener {
		void onContactList(List<Contact> contacts);
	}

	public interface StatusListener {
		void onStatus(String status);
	}

	public interface ErrorListener {
		void onError(String message);
	}

	private final WebSocketClient chatClient = new WebSocketClient();
	private final AudioClient audioClient = new AudioClient();
	private final com.securephone.client.video.VideoClient videoClient = new com.securephone.client.video.VideoClient();

	private AuthListener authListener;
	private ChatListener chatListener;
	private ContactListener contactListener;
	private StatusListener statusListener;
	private ErrorListener errorListener;

	private UserSession session = new UserSession();

	private String host = "localhost";
	private int chatPort = 8081;
	private int audioPort = 50000;
	private int videoPort = 50020;
	private int timeoutMs = 10000;
	private boolean enableTwoFaByDefault = true;

	public ConnectionManager() {
		loadConfig();
		chatClient.setMessageListener(this::handleMessage);
		chatClient.setConnectionListener(new WebSocketClient.ConnectionListener() {
			@Override
			public void onConnected() {
				if (statusListener != null) {
					statusListener.onStatus("connecte");
				}
			}

			@Override
			public void onDisconnected() {
				if (statusListener != null) {
					statusListener.onStatus("deconnecte");
				}
			}
		});
	}

	public void setAuthListener(AuthListener listener) {
		this.authListener = listener;
	}

	public void setChatListener(ChatListener listener) {
		this.chatListener = listener;
	}

	public void setContactListener(ContactListener listener) {
		this.contactListener = listener;
	}

	public void setStatusListener(StatusListener listener) {
		this.statusListener = listener;
	}

	public void setErrorListener(ErrorListener listener) {
		this.errorListener = listener;
	}

	public boolean connect() {
		try {
			chatClient.connect(host, chatPort, timeoutMs);
			return true;
		} catch (Exception e) {
			Logger.error("Echec connexion chat: " + e.getMessage());
			return false;
		}
	}

	public void disconnect() {
		chatClient.disconnect();
		audioClient.stopReceiving();
		videoClient.stop();
	}

	public void login(String username, String password, String totpCode) {
		try {
			ChatPacket packet = new ChatPacket(MessageType.LOGIN_REQUEST);
			JSONObject data = new JSONObject();
			data.put("username", username);
			data.put("password", password);
			if (totpCode != null && !totpCode.isEmpty()) {
				data.put("totp", totpCode);
			}
			packet.setData(data);
			chatClient.send(packet.toJson());
		} catch (Exception e) {
			if (authListener != null) {
				authListener.onLoginFailed("Erreur de connexion");
			}
		}
	}

	public void register(String username, String password) {
		try {
			ChatPacket packet = new ChatPacket(MessageType.REGISTER_REQUEST);
			JSONObject data = new JSONObject();
			data.put("username", username);
			data.put("password", password);
			data.put("enable_2fa", enableTwoFaByDefault);
			packet.setData(data);
			chatClient.send(packet.toJson());
		} catch (Exception e) {
			if (authListener != null) {
				authListener.onRegisterFailed("Erreur d'inscription");
			}
		}
	}

	public void sendChatMessage(String content, String receiverName) {
		if (!session.isLoggedIn()) {
			return;
		}
		try {
			ChatPacket packet = new ChatPacket(MessageType.TEXT_MESSAGE);
			JSONObject data = new JSONObject();
			data.put("sender_id", session.getUserId());
			data.put("sender_name", session.getUsername());
			if (receiverName != null && !receiverName.isEmpty()) {
				data.put("receiver_name", receiverName);
			}
			data.put("content", content);
			data.put("timestamp", System.currentTimeMillis());
			packet.setData(data);
			chatClient.send(packet.toJson());
		} catch (Exception e) {
			Logger.error("Erreur envoi message: " + e.getMessage());
		}
	}

	public void logout() {
		try {
			chatClient.send(new ChatPacket(MessageType.LOGOUT).toJson());
		} catch (Exception ignored) {
		}
		session = new UserSession();
		disconnect();
	}

	public void requestContacts() {
		if (!session.isLoggedIn()) {
			return;
		}
		try {
			ChatPacket packet = new ChatPacket(MessageType.CONTACT_LIST);
			JSONObject data = new JSONObject();
			data.put("action", "request");
			packet.setData(data);
			chatClient.send(packet.toJson());
		} catch (Exception e) {
			Logger.error("Erreur demande contacts: " + e.getMessage());
		}
	}

	public void startAudio() {
		if (!session.isLoggedIn()) {
			return;
		}
		try {
			audioClient.configure(host, audioPort, session.getUserId());
			audioClient.startReceiving();
			audioClient.startCapture();
		} catch (Exception e) {
			Logger.error("Erreur audio: " + e.getMessage());
		}
	}

	public void stopAudio() {
		audioClient.stopCapture();
	}

	public void startVideo() {
		if (!session.isLoggedIn()) {
			return;
		}
		try {
			videoClient.configure(host, videoPort, session.getUserId());
			videoClient.start();
		} catch (Exception e) {
			Logger.error("Erreur video: " + e.getMessage());
		}
	}

	public void stopVideo() {
		videoClient.stop();
	}

	public com.securephone.client.video.VideoClient getVideoClient() {
		return videoClient;
	}

	private void handleMessage(String message) {
		try {
			ChatPacket packet = ChatPacket.fromJson(message);
			MessageType type = packet.getType();
			JSONObject data = packet.getData();

			if (type == MessageType.LOGIN_RESPONSE) {
				handleLoginResponse(data);
				return;
			}

			if (type == MessageType.REGISTER_RESPONSE) {
				handleRegisterResponse(data);
				return;
			}

			if (type == MessageType.TEXT_MESSAGE) {
				String sender = data.optString("sender_name", "inconnu");
				String content = data.optString("content", "");
				long timestamp = data.optLong("timestamp", System.currentTimeMillis());
				ChatMessage chatMessage = new ChatMessage(sender, content, timestamp, "");
				if (chatListener != null) {
					chatListener.onMessage(chatMessage);
				}
				return;
			}

			if (type == MessageType.CONTACT_LIST) {
				JSONArray array = data.optJSONArray("contacts");
				List<Contact> contacts = new ArrayList<>();
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						String id = String.valueOf(obj.optInt("id"));
						String name = obj.optString("username");
						String status = obj.optString("status", "offline");
						contacts.add(new Contact(id, name, "", status, ""));
					}
				}
				if (contactListener != null) {
					contactListener.onContactList(contacts);
				}
				return;
			}

			if (type == MessageType.ERROR) {
				String error = data.optString("message", "Erreur");
				if (errorListener != null) {
					errorListener.onError(error);
				}
				return;
			}

		} catch (Exception e) {
			Logger.error("Erreur traitement message: " + e.getMessage());
		}
	}

	private void handleLoginResponse(JSONObject data) {
		String status = data.optString("status", "error");
		if ("2fa_required".equals(status)) {
			String baseMessage = data.optString("message", "2FA requis");
			String code = data.optString("code", "");
			String message = code.isEmpty() ? baseMessage : baseMessage + " (code: " + code + ")";
			if (authListener != null) {
				authListener.onLoginRequires2FA(message);
			}
			return;
		}

		if (!"success".equals(status)) {
			if (authListener != null) {
				authListener.onLoginFailed(data.optString("message", "Erreur login"));
			}
			return;
		}

		String sessionId = data.optString("session_id");
		int userId = data.optInt("user_id");
		String username = data.optString("username");
		session = new UserSession(sessionId, userId, username);

		if (authListener != null) {
			authListener.onLoginSuccess(session);
		}
	}

	private void handleRegisterResponse(JSONObject data) {
		String status = data.optString("status", "error");
		if ("success".equals(status)) {
			if (authListener != null) {
				authListener.onRegisterSuccess(data.optString("message", "Inscription OK"));
			}
		} else {
			if (authListener != null) {
				authListener.onRegisterFailed(data.optString("message", "Erreur inscription"));
			}
		}
	}

	private void loadConfig() {
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream("client/resources/config.properties")) {
			props.load(fis);
		} catch (Exception e) {
			Logger.warn("Config manquante, valeurs par defaut utilisees");
		}

		host = props.getProperty("server.host", host);
		timeoutMs = parseInt(props.getProperty("websocket.timeout"), timeoutMs);

		String wsUrl = props.getProperty("websocket.url", "ws://" + host + ":" + chatPort + "/chat");
		try {
			URI uri = URI.create(wsUrl.replace("ws://", "http://").replace("wss://", "https://"));
			if (uri.getHost() != null) {
				host = uri.getHost();
			}
			if (uri.getPort() > 0) {
				chatPort = uri.getPort();
			}
		} catch (Exception e) {
			Logger.warn("URL websocket invalide, fallback sur host/port par defaut");
		}

		audioPort = parseInt(props.getProperty("udp.audio.port.start"), audioPort);
		videoPort = parseInt(props.getProperty("udp.video.port.start"), videoPort);
		enableTwoFaByDefault = Boolean.parseBoolean(props.getProperty("security.2fa.enabled", "true"));
	}

	private int parseInt(String value, int fallback) {
		if (value == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
