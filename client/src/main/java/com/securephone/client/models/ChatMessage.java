package com.securephone.client.models;

/**
 * ChatMessage - Modèle d'un message de chat
 * 
 * Représente un message dans une conversation
 * 
 * @author Hatsu
 */
public class ChatMessage {
    
    private String sender;
    private String content;
    private long timestamp;
    private String token;
    
    // ========== CONSTRUCTEUR ==========
    public ChatMessage(String sender, String content, long timestamp, String token) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.token = token;
    }
    
    // ========== GETTERS ==========
    public String getSender() {
        return sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public long getTimestampMillis() {
        return timestamp;
    }
    
    public String getTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    public String getToken() {
        return token;
    }
    
    // ========== SETTERS ==========
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    @Override
    public String toString() {
        return "[" + getTimestamp() + "] " + sender + ": " + content;
    }
}
