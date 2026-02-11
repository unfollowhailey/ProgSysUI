package com.securephone.server.network;

import com.securephone.shared.protocol.ChatPacket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketManager {

	private final PacketRouter router;
	private ServerSocket serverSocket;
	private Thread acceptThread;
	private volatile boolean running;
	private int port = 8081;

	public SocketManager(PacketRouter router) {
		this.router = router;
		this.port = PacketRouter.loadIntConfig("websocket.port", 8081);
	}

	public void start() throws Exception {
		if (running) {
			return;
		}
		serverSocket = new ServerSocket(port);
		running = true;

		acceptThread = new Thread(this::acceptLoop, "SocketAcceptThread");
		acceptThread.setDaemon(true);
		acceptThread.start();
	}

	public void stop() {
		running = false;
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (Exception ignored) {
		}
	}

	private void acceptLoop() {
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				ClientHandler handler = new ClientHandler(socket, router);
				router.register(handler);
				handler.start();
			} catch (Exception e) {
				if (!running) {
					break;
				}
			}
		}
	}

	private static class ClientHandler implements PacketRouter.ClientConnection {

		private final Socket socket;
		private final PacketRouter router;
		private BufferedReader reader;
		private BufferedWriter writer;
		private Thread readThread;
		private int userId = -1;
		private String username;

		ClientHandler(Socket socket, PacketRouter router) throws Exception {
			this.socket = socket;
			this.router = router;
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		}

		void start() {
			readThread = new Thread(this::readLoop, "ClientReadThread");
			readThread.setDaemon(true);
			readThread.start();
		}

		private void readLoop() {
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					ChatPacket packet = ChatPacket.fromJson(line);
					router.handlePacket(packet, this);
				}
			} catch (Exception ignored) {
			} finally {
				router.unregister(this);
				close();
			}
		}

		private void close() {
			try {
				reader.close();
			} catch (Exception ignored) {
			}
			try {
				writer.close();
			} catch (Exception ignored) {
			}
			try {
				socket.close();
			} catch (Exception ignored) {
			}
		}

		@Override
		public synchronized void send(ChatPacket packet) {
			try {
				writer.write(packet.toJson());
				writer.newLine();
				writer.flush();
			} catch (Exception ignored) {
			}
		}

		@Override
		public int getUserId() {
			return userId;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public void setUser(int userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		@Override
		public boolean isAuthenticated() {
			return userId > 0;
		}
	}
}
