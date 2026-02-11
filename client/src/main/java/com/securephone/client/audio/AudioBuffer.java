package com.securephone.client.audio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe buffer for audio frames.
 */
public class AudioBuffer {

	private final BlockingQueue<byte[]> queue;

	public AudioBuffer(int capacity) {
		this.queue = new LinkedBlockingQueue<>(capacity);
	}

	public boolean push(byte[] frame) {
		return queue.offer(frame);
	}

	public boolean push(byte[] frame, long timeoutMs) throws InterruptedException {
		return queue.offer(frame, timeoutMs, TimeUnit.MILLISECONDS);
	}

	public byte[] poll() {
		return queue.poll();
	}

	public byte[] poll(long timeoutMs) throws InterruptedException {
		return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
	}

	public void clear() {
		queue.clear();
	}

	public int size() {
		return queue.size();
	}

	public void add(byte[] data) {
		push(data);
	}
}
