package com.securephone.client.webpush;

import java.util.UUID;

public class PushClient {

	private String deviceToken;
	private String platform = "desktop";

	public String register() {
		if (deviceToken == null || deviceToken.isEmpty()) {
			deviceToken = UUID.randomUUID().toString();
		}
		return deviceToken;
	}

	public void unregister() {
		deviceToken = null;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		if (platform != null && !platform.isEmpty()) {
			this.platform = platform;
		}
	}
}
