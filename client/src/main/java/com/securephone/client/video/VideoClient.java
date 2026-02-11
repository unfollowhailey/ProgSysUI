package com.securephone.client.video;

import com.securephone.shared.protocol.VideoPacket;

import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoClient {

	public interface FrameListener {
		void onFrame(BufferedImage image);
	}

	private final H264Codec codec = new H264Codec();
	private final VideoCapture capture = new VideoCapture();

	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int serverPort;
	private int userId;
	private int width = 640;
	private int height = 480;
	private int fps = 15;
	private volatile boolean running;
	private Thread receiveThread;
	private final AtomicInteger frameCounter = new AtomicInteger();
	private FrameListener frameListener;

	public void configure(String host, int port, int userId) throws Exception {
		this.serverAddress = InetAddress.getByName(host);
		this.serverPort = port;
		this.userId = userId;
		if (socket == null || socket.isClosed()) {
			socket = new DatagramSocket();
		}
	}

	public void setVideoSettings(int width, int height, int fps) {
		this.width = width;
		this.height = height;
		this.fps = fps;
	}

	public void setFrameListener(FrameListener listener) {
		this.frameListener = listener;
	}

	public void start() {
		if (running) {
			return;
		}
		running = true;
		startReceiver();
		capture.start(width, height, fps, this::sendFrame);
	}

	public void stop() {
		running = false;
		capture.stop();
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}

	private void sendFrame(BufferedImage image) {
		try {
			byte[] encoded = codec.encode(image);
			VideoPacket packet = new VideoPacket(userId, encoded, encoded.length, width, height, 0);
			packet.setFrameNumber(frameCounter.incrementAndGet());
			byte[] data = packet.toBytes();
			DatagramPacket datagram = new DatagramPacket(data, data.length, serverAddress, serverPort);
			socket.send(datagram);
		} catch (Exception e) {
			// ignore transient frame send errors
		}
	}

	private void startReceiver() {
		receiveThread = new Thread(() -> {
			byte[] buffer = new byte[VideoPacket.MAX_VIDEO_SIZE + 64];
			while (running && socket != null && !socket.isClosed()) {
				try {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					byte[] data = new byte[packet.getLength()];
					System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
					VideoPacket videoPacket = VideoPacket.fromBytes(data);
					if (videoPacket.getUserId() == userId) {
						continue;
					}
					BufferedImage image = codec.decode(videoPacket.getVideoData());
					if (frameListener != null && image != null) {
						frameListener.onFrame(image);
					}
				} catch (Exception e) {
					if (!running) {
						break;
					}
				}
			}
		}, "VideoReceiveThread");
		receiveThread.setDaemon(true);
		receiveThread.start();
	}
}
