package com.securephone.client.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioCapture {

	public interface AudioFrameListener {
		void onAudioFrame(byte[] data, int length);
	}

	private static final float SAMPLE_RATE = 48000f;
	private static final int SAMPLE_SIZE = 16;
	private static final int CHANNELS = 1;
	private static final boolean SIGNED = true;
	private static final boolean LITTLE_ENDIAN = false;

	private static final int FRAME_SAMPLES = 960;

	private TargetDataLine line;
	private Thread captureThread;
	private volatile boolean running;
	private final AudioBuffer buffer;

	public AudioCapture() {
		this.buffer = null;
	}

	public AudioCapture(AudioBuffer buffer) {
		this.buffer = buffer;
	}

	public AudioFormat getFormat() {
		return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, LITTLE_ENDIAN);
	}

	public void start() throws Exception {
		start((data, length) -> {
			if (buffer != null) {
				buffer.push(data);
			}
		});
	}

	public void start(AudioFrameListener listener) throws Exception {
		if (running) {
			return;
		}

		AudioFormat format = getFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();

		running = true;
		captureThread = new Thread(() -> captureLoop(listener), "AudioCaptureThread");
		captureThread.setDaemon(true);
		captureThread.start();
	}

	private void captureLoop(AudioFrameListener listener) {
		int frameBytes = FRAME_SAMPLES * (SAMPLE_SIZE / 8) * CHANNELS;
		byte[] buffer = new byte[frameBytes];

		while (running) {
			int read = line.read(buffer, 0, buffer.length);
			if (read > 0 && listener != null) {
				byte[] frame = new byte[read];
				System.arraycopy(buffer, 0, frame, 0, read);
				listener.onAudioFrame(frame, read);
			}
		}
	}

	public void stop() {
		running = false;
		if (line != null) {
			line.stop();
			line.close();
			line = null;
		}
	}

	public boolean isRunning() {
		return running;
	}
}
