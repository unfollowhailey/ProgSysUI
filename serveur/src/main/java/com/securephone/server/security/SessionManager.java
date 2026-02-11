package com.securephone.server.security;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private static final Map<String, Session> sessions = new HashMap<>();
    public static class Session {
        public final int userId;
        public final String username;
        public final long createdAt;
        public long lastActivity;
        Session(int userId, String username) {
            this.userId = userId;
            this.username = username;
            this.createdAt = System.currentTimeMillis();
            this.lastActivity = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastActivity > 3600000; // 1 heure
        }
    }

    public static String createSession(int userId, String username) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new Session(userId, username));
        return sessionId;
    }

    public static Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            session.lastActivity = System.currentTimeMillis();
            return session;
        }
        return null;
    }

    public static void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
