package com.securephone.server.network;

import com.securephone.server.database.DatabaseManager;
import com.securephone.server.database.UserDAO;
import com.securephone.server.models.User;
import com.securephone.server.security.PasswordHasher;
import com.securephone.server.security.SessionManager;
import com.securephone.server.security.Simple2FA;
import com.securephone.shared.protocol.ChatPacket;
import com.securephone.shared.protocol.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketRouter {

	public interface ClientConnection {
		void send(ChatPacket packet);
		int getUserId();
		String getUsername();
		void setUser(int userId, String username);
		boolean isAuthenticated();
	}

	private static final AtomicInteger USER_ID_SEQUENCE = new AtomicInteger(1000);

	private final Map<Integer, ClientConnection> connectedUsers = new ConcurrentHashMap<>();
	private final Map<String, UserRecord> usersByName = new ConcurrentHashMap<>();
	private final Set<ClientConnection> allConnections = ConcurrentHashMap.newKeySet();
	private final boolean defaultTwoFaEnabled;
	private final boolean devMode;
	private final UserDAO userDAO;
	private final boolean dbAvailable;

	public PacketRouter() {
		this.defaultTwoFaEnabled = loadBoolConfig("security.2fa.enabled", true);
		this.devMode = loadBoolConfig("dev.mode", false);
		UserDAO dao = null;
		boolean available = false;
		try {
			DatabaseManager dbManager = DatabaseManager.getInstance();
			dao = new UserDAO(dbManager.getConnection());
			available = true;
		} catch (Exception ignored) {
		}
		this.userDAO = dao;
		this.dbAvailable = available;
		seedDefaultUsers();
	}

	public void register(ClientConnection connection) {
		allConnections.add(connection);
	}

	public void unregister(ClientConnection connection) {
		allConnections.remove(connection);
		if (connection.isAuthenticated()) {
			connectedUsers.remove(connection.getUserId());
			broadcastPresence(connection.getUserId(), "offline");
		}
	}

	public void handlePacket(ChatPacket packet, ClientConnection connection) {
		if (packet == null) {
			return;
		}

		MessageType type = packet.getType();
		JSONObject data = packet.getData();

		switch (type) {
			case LOGIN_REQUEST:
				handleLogin(data, connection);
				break;
			case REGISTER_REQUEST:
				handleRegister(data, connection);
				break;
			case TEXT_MESSAGE:
				handleChatMessage(data, connection);
				break;
			case CONTACT_LIST:
				handleContactList(connection);
				break;
			case PING:
				connection.send(new ChatPacket(MessageType.PONG));
				break;
			case LOGOUT:
				handleLogout(connection);
				break;
			default:
				break;
		}
	}

	private void handleLogin(JSONObject data, ClientConnection connection) {
		String username = data.optString("username", "");
		String password = data.optString("password", "");
		String totp = data.optString("totp", "");

		UserRecord record = usersByName.get(username);
		if (record == null && dbAvailable) {
			record = loadUserFromDatabase(username);
		}
		if (record == null) {
			sendLoginResponse(connection, "error", "Utilisateur inconnu", null);
			return;
		}

		if (!PasswordHasher.verify(password, record.passwordHash)) {
			sendLoginResponse(connection, "error", "Mot de passe incorrect", null);
			return;
		}

		if (record.twofaEnabled) {
			if (totp == null || totp.isEmpty()) {
				String code = Simple2FA.generateCode(username);
				JSONObject response = new JSONObject();
				response.put("status", "2fa_required");
				response.put("message", "Code 2FA requis");
				response.put("code", code);
				connection.send(new ChatPacket(MessageType.LOGIN_RESPONSE, response));
				return;
			}
			if (!Simple2FA.verifyCode(username, totp)) {
				sendLoginResponse(connection, "error", "Code 2FA invalide", null);
				return;
			}
		}

		String sessionId = SessionManager.createSession(record.userId, record.username);
		connection.setUser(record.userId, record.username);
		connectedUsers.put(record.userId, connection);
		record.status = "online";
		if (dbAvailable) {
			try {
				userDAO.updateStatus(record.userId, "online");
			} catch (Exception ignored) {
			}
		}

		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("session_id", sessionId);
		response.put("user_id", record.userId);
		response.put("username", record.username);

		ChatPacket packet = new ChatPacket(MessageType.LOGIN_RESPONSE, response);
		connection.send(packet);
		broadcastPresence(record.userId, "online");
	}

	private void handleRegister(JSONObject data, ClientConnection connection) {
		String username = data.optString("username", "");
		String password = data.optString("password", "");
		boolean enableTwoFa = data.has("enable_2fa")
			? data.optBoolean("enable_2fa", defaultTwoFaEnabled)
			: defaultTwoFaEnabled;
		String email = data.optString("email", "");

		if (username.isEmpty() || password.isEmpty()) {
			sendRegisterResponse(connection, "error", "Champs manquants");
			return;
		}

		if (usersByName.containsKey(username)) {
			sendRegisterResponse(connection, "error", "Utilisateur deja existant");
			return;
		}

		if (dbAvailable) {
			try {
				if (userDAO.findByUsername(username) != null) {
					sendRegisterResponse(connection, "error", "Utilisateur deja existant");
					return;
				}
				if (email.isEmpty()) {
					email = username + "@securephone.local";
				}
				boolean created = userDAO.createUser(username, PasswordHasher.hash(password), email);
				if (!created) {
					sendRegisterResponse(connection, "error", "Erreur creation utilisateur");
					return;
				}
				User dbUser = userDAO.findByUsername(username);
				if (dbUser == null) {
					sendRegisterResponse(connection, "error", "Utilisateur introuvable apres creation");
					return;
				}
				UserRecord record = new UserRecord(dbUser.getId(), username, dbUser.getPasswordHash(), enableTwoFa);
				record.status = dbUser.getStatus() != null ? dbUser.getStatus() : "offline";
				usersByName.put(username, record);
				sendRegisterResponse(connection, "success", "Inscription reussie");
				return;
			} catch (Exception e) {
				sendRegisterResponse(connection, "error", "Erreur base de donnees");
				return;
			}
		}

		int userId = USER_ID_SEQUENCE.incrementAndGet();
		UserRecord record = new UserRecord(userId, username, PasswordHasher.hash(password), enableTwoFa);
		usersByName.put(username, record);
		sendRegisterResponse(connection, "success", "Inscription reussie");
	}

	private void handleChatMessage(JSONObject data, ClientConnection connection) {
		if (!connection.isAuthenticated()) {
			return;
		}

		String receiverName = data.optString("receiver_name", "");

		JSONObject payload = new JSONObject();
		payload.put("sender_id", connection.getUserId());
		payload.put("sender_name", connection.getUsername());
		payload.put("content", data.optString("content", ""));
		payload.put("timestamp", System.currentTimeMillis());

		ChatPacket packet = new ChatPacket(MessageType.TEXT_MESSAGE, payload);
		if (receiverName != null && !receiverName.isEmpty()) {
			ClientConnection receiver = findByUsername(receiverName);
			if (receiver != null) {
				receiver.send(packet);
			} else {
				sendError(connection, "Utilisateur hors ligne");
			}
			return;
		}
		broadcast(packet, connection.getUserId());
	}

	private ClientConnection findByUsername(String username) {
		for (ClientConnection connection : allConnections) {
			if (username.equalsIgnoreCase(connection.getUsername())) {
				return connection;
			}
		}
		return null;
	}

	private void sendError(ClientConnection connection, String message) {
		JSONObject payload = new JSONObject();
		payload.put("message", message);
		connection.send(new ChatPacket(MessageType.ERROR, payload));
	}

	private void handleContactList(ClientConnection connection) {
		JSONArray contacts = new JSONArray();
		for (UserRecord record : usersByName.values()) {
			JSONObject obj = new JSONObject();
			obj.put("id", record.userId);
			obj.put("username", record.username);
			obj.put("status", record.status);
			contacts.put(obj);
		}

		JSONObject payload = new JSONObject();
		payload.put("contacts", contacts);
		connection.send(new ChatPacket(MessageType.CONTACT_LIST, payload));
	}

	private void handleLogout(ClientConnection connection) {
		if (connection.isAuthenticated()) {
			connectedUsers.remove(connection.getUserId());
			broadcastPresence(connection.getUserId(), "offline");
		}
	}

	private void broadcastPresence(int userId, String status) {
		JSONObject payload = new JSONObject();
		payload.put("user_id", userId);
		payload.put("status", status);
		ChatPacket packet = new ChatPacket(MessageType.PRESENCE_UPDATE, payload);
		broadcast(packet, userId);
	}

	private void broadcast(ChatPacket packet, int excludeUserId) {
		for (ClientConnection connection : allConnections) {
			if (connection.getUserId() == excludeUserId) {
				continue;
			}
			connection.send(packet);
		}
	}

	private void sendLoginResponse(ClientConnection connection, String status, String message, String sessionId) {
		JSONObject response = new JSONObject();
		response.put("status", status);
		response.put("message", message);
		if (sessionId != null) {
			response.put("session_id", sessionId);
		}
		ChatPacket packet = new ChatPacket(MessageType.LOGIN_RESPONSE, response);
		connection.send(packet);
	}

	private void sendRegisterResponse(ClientConnection connection, String status, String message) {
		JSONObject response = new JSONObject();
		response.put("status", status);
		response.put("message", message);
		ChatPacket packet = new ChatPacket(MessageType.REGISTER_RESPONSE, response);
		connection.send(packet);
	}

	private void seedDefaultUsers() {
		if (dbAvailable) {
			return;
		}
		int userId = USER_ID_SEQUENCE.incrementAndGet();
		UserRecord user = new UserRecord(userId, "test", PasswordHasher.hash("test"), defaultTwoFaEnabled);
		usersByName.put(user.username, user);
	}

	private UserRecord loadUserFromDatabase(String username) {
		if (!dbAvailable) {
			return null;
		}
		try {
			User user = userDAO.findByUsername(username);
			if (user == null) {
				return null;
			}
			UserRecord record = new UserRecord(user.getId(), user.getUsername(), user.getPasswordHash(), user.isTwofaEnabled());
			record.status = user.getStatus() != null ? user.getStatus() : "offline";
			usersByName.put(user.getUsername(), record);
			return record;
		} catch (Exception e) {
			return null;
		}
	}

	public static int loadIntConfig(String key, int fallback) {
		Properties props = new Properties();
		loadProperties(props);

		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public static boolean loadBoolConfig(String key, boolean fallback) {
		Properties props = new Properties();
		loadProperties(props);

		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		return value.trim().equalsIgnoreCase("true");
	}

	private static void loadProperties(Properties props) {
		try (InputStream in = PacketRouter.class.getClassLoader()
			.getResourceAsStream("com/securephone/resources/config.properties")) {
			if (in != null) {
				props.load(in);
				return;
			}
		} catch (Exception ignored) {
		}

		String[] candidates = new String[] {
			"serveur/src/main/java/com/securephone/resources/config.properties",
			"serveur/src/main/resources/config.properties",
			"config.properties"
		};
		for (String path : candidates) {
			try (InputStream fileIn = new FileInputStream(path)) {
				props.load(fileIn);
				return;
			} catch (Exception ignored) {
			}
		}
	}

	private static class UserRecord {
		int userId;
		String username;
		String passwordHash;
		boolean twofaEnabled;
		String status = "offline";

		UserRecord(int userId, String username, String passwordHash, boolean twofaEnabled) {
			this.userId = userId;
			this.username = username;
			this.passwordHash = passwordHash;
			this.twofaEnabled = twofaEnabled;
		}
	}
}
