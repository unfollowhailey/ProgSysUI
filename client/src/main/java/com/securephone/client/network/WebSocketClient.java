package com.securephone.client.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WebSocketClient {

	public interface MessageListener {
		void onMessage(String message);
	}

	public interface ConnectionListener {
		void onConnected();
		void onDisconnected();
	}

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Thread readThread;
	private MessageListener messageListener;
	private ConnectionListener connectionListener;
	private volatile boolean running;

	public void setMessageListener(MessageListener listener) {
		this.messageListener = listener;
	}

	public void setConnectionListener(ConnectionListener listener) {
		this.connectionListener = listener;
	}

	public void connect(String host, int port, int timeoutMs) throws Exception {
		if (running) {
			return;
		}
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), timeoutMs);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		running = true;

		if (connectionListener != null) {
			connectionListener.onConnected();
		}

		readThread = new Thread(this::readLoop, "ChatSocketReadThread");
		readThread.setDaemon(true);
		readThread.start();
	}

	private void readLoop() {
		try {
			String line;
			while (running && (line = reader.readLine()) != null) {
				if (messageListener != null) {
					messageListener.onMessage(line);
				}
			}
		} catch (Exception e) {
			// ignore read errors
		} finally {
			disconnect();
		}
	}

	public synchronized void send(String message) throws Exception {
		if (!running || writer == null) {
			return;
		}
		writer.write(message);
		writer.newLine();
		writer.flush();
	}

	public void disconnect() {
		running = false;
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception ignored) {
		}
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (Exception ignored) {
		}
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception ignored) {
		}

		if (connectionListener != null) {
			connectionListener.onDisconnected();
		}
	}

	public boolean isConnected() {
		return running && socket != null && socket.isConnected() && !socket.isClosed();
	}
}
