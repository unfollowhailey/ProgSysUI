package com.securephone.server.udp;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomAudioManager {

	private final ConcurrentHashMap<Integer, Set<Integer>> rooms = new ConcurrentHashMap<>();

	public void joinRoom(int roomId, int userId) {
		rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(userId);
	}

	public void leaveRoom(int roomId, int userId) {
		Set<Integer> members = rooms.get(roomId);
		if (members != null) {
			members.remove(userId);
			if (members.isEmpty()) {
				rooms.remove(roomId);
			}
		}
	}

	public Set<Integer> getMembers(int roomId) {
		return rooms.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
	}
}
