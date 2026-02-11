package com.securephone.server.udp;

import com.securephone.server.network.PacketRouter;
import com.securephone.shared.protocol.VideoPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VideoServer {

	private final VideoPacketHandler handler = new VideoPacketHandler();
	private final Map<String, InetSocketAddress> clients = new ConcurrentHashMap<>();
	private DatagramSocket socket;
	private Thread serverThread;
	private volatile boolean running;
	private int port = 50020;

	public VideoServer() {
		this.port = PacketRouter.loadIntConfig("video.udp.port", 50020);
	}

	public void start() throws Exception {
		if (running) {
			return;
		}
		socket = new DatagramSocket(port);
		running = true;
		serverThread = new Thread(this::runLoop, "VideoServerThread");
		serverThread.setDaemon(true);
		serverThread.start();
	}

	public void stop() {
		running = false;
		if (socket != null) {
			socket.close();
		}
	}

	private void runLoop() {
		byte[] buffer = new byte[VideoPacket.MAX_VIDEO_SIZE + 128];
		while (running) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				VideoPacket videoPacket = handler.parse(packet.getData(), packet.getLength());
				if (videoPacket == null) {
					continue;
				}

				String key = packet.getAddress().getHostAddress() + ":" + packet.getPort();
				clients.put(key, new InetSocketAddress(packet.getAddress(), packet.getPort()));

				broadcast(packet.getData(), packet.getLength(), key);
			} catch (Exception e) {
				if (!running) {
					break;
				}
			}
		}
	}

	private void broadcast(byte[] data, int length, String senderKey) {
		for (Map.Entry<String, InetSocketAddress> entry : clients.entrySet()) {
			if (entry.getKey().equals(senderKey)) {
				continue;
			}
			try {
				InetSocketAddress address = entry.getValue();
				DatagramPacket out = new DatagramPacket(data, length, address.getAddress(), address.getPort());
				socket.send(out);
			} catch (Exception ignored) {
			}
		}
	}
}
