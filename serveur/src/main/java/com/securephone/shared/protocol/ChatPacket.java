package com.securephone.shared.protocol;

import org.json.JSONObject;

public class ChatPacket {
    private MessageType type;
    private JSONObject data;
    private long timestamp;

    public ChatPacket() {
        this.timestamp = System.currentTimeMillis();
        this.data = new JSONObject();
    }

    public ChatPacket(MessageType type) {
        this();
        this.type = type;
    }

    public ChatPacket(MessageType type, JSONObject data) {
        this();
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type.name());
        json.put("data", data);
        json.put("timestamp", timestamp);
        return json.toString();
    }

    public static ChatPacket fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        ChatPacket packet = new ChatPacket();
        packet.type = MessageType.valueOf(json.getString("type"));
        packet.data = json.getJSONObject("data");
        packet.timestamp = json.getLong("timestamp");
        return packet;
    }
}
