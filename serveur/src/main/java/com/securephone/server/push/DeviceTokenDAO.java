package com.securephone.server.push;

import com.securephone.server.database.DatabaseManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceTokenDAO {

	private final com.securephone.server.database.DeviceTokenDAO dbDao;
	private final boolean dbAvailable;
	private final Map<Integer, String> memoryTokens = new ConcurrentHashMap<>();

	public DeviceTokenDAO() {
		com.securephone.server.database.DeviceTokenDAO dao = null;
		boolean available = false;
		try {
			dao = new com.securephone.server.database.DeviceTokenDAO(
				DatabaseManager.getInstance().getConnection());
			available = true;
		} catch (Exception ignored) {
		}
		this.dbDao = dao;
		this.dbAvailable = available;
	}

	public boolean saveToken(int userId, String deviceToken, String platform) {
		if (dbAvailable) {
			try {
				return dbDao.saveToken(userId, deviceToken, platform);
			} catch (Exception ignored) {
			}
		}
		memoryTokens.put(userId, deviceToken);
		return true;
	}

	public String getDeviceToken(int userId) {
		if (dbAvailable) {
			try {
				return dbDao.getDeviceToken(userId);
			} catch (Exception ignored) {
			}
		}
		return memoryTokens.get(userId);
	}

	public boolean deactivateToken(String deviceToken) {
		if (dbAvailable) {
			try {
				return dbDao.deactivateToken(deviceToken);
			} catch (Exception ignored) {
			}
		}
		return memoryTokens.values().removeIf(token -> token.equals(deviceToken));
	}
}
