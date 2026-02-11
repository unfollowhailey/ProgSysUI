package com.securephone.client.webpush;

public class ServiceWorker {

	public boolean isSupported() {
		return false;
	}

	public void start() {
		// Desktop client: no service worker required
	}

	public void stop() {
		// Desktop client: no service worker required
	}
}
