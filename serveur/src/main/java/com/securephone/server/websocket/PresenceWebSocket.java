package com.securephone.server.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/presence")
public class PresenceWebSocket {

	private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		broadcast(message);
	}

	private void broadcast(String message) {
		for (Session session : sessions) {
			if (session.isOpen()) {
				session.getAsyncRemote().sendText(message);
			}
		}
	}
}
