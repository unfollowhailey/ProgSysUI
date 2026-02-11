package com.securephone.client.video;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class VideoCapture {

	public interface VideoFrameListener {
		void onFrame(BufferedImage image);
	}

	private Thread captureThread;
	private volatile boolean running;

	public void start(int width, int height, int fps, VideoFrameListener listener) {
		if (running) {
			return;
		}
		running = true;
		captureThread = new Thread(() -> captureLoop(width, height, fps, listener), "VideoCaptureThread");
		captureThread.setDaemon(true);
		captureThread.start();
	}

	private void captureLoop(int width, int height, int fps, VideoFrameListener listener) {
		long frameDelay = Math.max(1, 1000 / Math.max(1, fps));
		int tick = 0;

		while (running) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = image.createGraphics();

			Color bg = new Color(20 + (tick % 200), 50, 90);
			g.setColor(bg);
			g.fillRect(0, 0, width, height);

			g.setColor(Color.WHITE);
			g.setFont(new Font("SansSerif", Font.BOLD, 20));
			g.drawString("SecurePhone Video", 20, 30);
			g.setFont(new Font("SansSerif", Font.PLAIN, 14));
			g.drawString("Frame: " + tick, 20, 55);
			g.drawString("Time: " + System.currentTimeMillis(), 20, 75);

			g.dispose();

			if (listener != null) {
				listener.onFrame(image);
			}

			tick++;

			try {
				Thread.sleep(frameDelay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void stop() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}
}
