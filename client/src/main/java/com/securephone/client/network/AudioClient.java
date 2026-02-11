package com.securephone.client.network;

import com.securephone.client.audio.AudioCapture;
import com.securephone.client.audio.AudioPlayer;
import com.securephone.client.audio.OpusCodec;
import com.securephone.shared.protocol.AudioPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioClient {

	private final OpusCodec codec = new OpusCodec();
	private final AudioCapture capture = new AudioCapture();
	private final AudioPlayer player = new AudioPlayer();
	private final AtomicInteger sequence = new AtomicInteger();

	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int serverPort;
	private int userId;
	private volatile boolean receiving;
	private Thread receiveThread;

	public void configure(String host, int port, int userId) throws Exception {
		this.serverAddress = InetAddress.getByName(host);
		this.serverPort = port;
		this.userId = userId;
		if (socket == null || socket.isClosed()) {
			socket = new DatagramSocket();
		}
		player.start(capture.getFormat());
	}

	public void startReceiving() {
		if (receiving) {
			return;
		}
		receiving = true;
		receiveThread = new Thread(this::receiveLoop, "AudioReceiveThread");
		receiveThread.setDaemon(true);
		receiveThread.start();
	}

	public void stopReceiving() {
		receiving = false;
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
		player.stop();
	}

	public void startCapture() throws Exception {
		capture.start((data, length) -> sendAudio(data, length));
	}

	public void stopCapture() {
		capture.stop();
	}

	private void sendAudio(byte[] data, int length) {
		try {
			byte[] encoded = codec.encode(data);
			AudioPacket packet = new AudioPacket(userId, encoded, encoded.length);
			packet.setSequenceNumber(sequence.incrementAndGet());
			byte[] bytes = packet.toBytes();
			DatagramPacket datagram = new DatagramPacket(bytes, bytes.length, serverAddress, serverPort);
			socket.send(datagram);
		} catch (Exception e) {
			// ignore transient send errors
		}
	}

	private void receiveLoop() {
		byte[] buffer = new byte[AudioPacket.MAX_AUDIO_SIZE + 64];
		while (receiving && socket != null && !socket.isClosed()) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				byte[] data = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
				AudioPacket audioPacket = AudioPacket.fromBytes(data);
				if (audioPacket.getUserId() == userId) {
					continue;
				}
				byte[] pcm = codec.decode(audioPacket.getAudioData());
				player.play(pcm, pcm.length);
			} catch (Exception e) {
				if (!receiving) {
					break;
				}
			}
		}
	}
}
