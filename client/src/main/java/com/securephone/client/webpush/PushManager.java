package com.securephone.client.webpush;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PushManager {

	public interface PushListener {
		void onPush(String title, String body, String type);
	}

	private final PushClient pushClient = new PushClient();
	private final List<PushListener> listeners = new CopyOnWriteArrayList<>();

	public String registerDevice() {
		return pushClient.register();
	}

	public void unregisterDevice() {
		pushClient.unregister();
	}

	public String getDeviceToken() {
		return pushClient.getDeviceToken();
	}

	public void addListener(PushListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(PushListener listener) {
		listeners.remove(listener);
	}

	public void simulateIncoming(String title, String body, String type) {
		for (PushListener listener : listeners) {
			listener.onPush(title, body, type);
		}
	}
}
