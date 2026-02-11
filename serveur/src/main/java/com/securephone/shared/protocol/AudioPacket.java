package com.securephone.shared.protocol;

import java.nio.ByteBuffer;

public class AudioPacket {
    public static final int MAX_AUDIO_SIZE = 4096;
    private static final int HEADER_SIZE = 20;

    private int userId;
    private int sequenceNumber;
    private long timestamp;
    private byte[] audioData;
    private int dataLength;

    public AudioPacket() {}

    public AudioPacket(int userId, byte[] audioData, int dataLength) {
        this.userId = userId;
        this.audioData = audioData;
        this.dataLength = dataLength;
        this.timestamp = System.currentTimeMillis();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + dataLength);
        buffer.putInt(userId);
        buffer.putInt(sequenceNumber);
        buffer.putLong(timestamp);
        buffer.putInt(dataLength);
        buffer.put(audioData, 0, dataLength);
        return buffer.array();
    }

    public static AudioPacket fromBytes(byte[] data) {
        if (data == null || data.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Audio packet too small");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        AudioPacket packet = new AudioPacket();
        packet.userId = buffer.getInt();
        packet.sequenceNumber = buffer.getInt();
        packet.timestamp = buffer.getLong();
        packet.dataLength = buffer.getInt();

        if (packet.dataLength > MAX_AUDIO_SIZE) {
            throw new IllegalArgumentException("Audio data too large: " + packet.dataLength);
        }

        if (data.length < HEADER_SIZE + packet.dataLength) {
            throw new IllegalArgumentException("Audio packet data incomplete");
        }

        packet.audioData = new byte[packet.dataLength];
        buffer.get(packet.audioData, 0, packet.dataLength);
        return packet;
    }

    public int getSize() {
        return HEADER_SIZE + dataLength;
    }
}
