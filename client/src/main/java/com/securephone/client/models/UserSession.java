package com.securephone.client.models;

public class UserSession {
	private String sessionId;
	private int userId;
	private String username;
	private String status;
	private long loginTime;

	public UserSession() {
		this.status = "offline";
	}

	public UserSession(String sessionId, int userId, String username) {
		this.sessionId = sessionId;
		this.userId = userId;
		this.username = username;
		this.status = "online";
		this.loginTime = System.currentTimeMillis();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public boolean isLoggedIn() {
		return sessionId != null && !sessionId.isEmpty();
	}

	@Override
	public String toString() {
		return String.format("UserSession{userId=%d, username='%s', status='%s'}", userId, username, status);
	}
}
