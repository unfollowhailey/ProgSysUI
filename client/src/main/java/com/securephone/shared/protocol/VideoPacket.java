package com.securephone.shared.protocol;

import java.nio.ByteBuffer;

public class VideoPacket {
    public static final int MAX_VIDEO_SIZE = 65536;
    private static final int HEADER_SIZE = 32;

    private int userId;
    private int frameNumber;
    private int frameType;
    private long timestamp;
    private int width;
    private int height;
    private byte[] videoData;
    private int dataLength;

    public VideoPacket() {}

    public VideoPacket(int userId, byte[] videoData, int dataLength,
                      int width, int height, int frameType) {
        this.userId = userId;
        this.videoData = videoData;
        this.dataLength = dataLength;
        this.width = width;
        this.height = height;
        this.frameType = frameType;
        this.timestamp = System.currentTimeMillis();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public int getFrameType() {
        return frameType;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getVideoData() {
        return videoData;
    }

    public void setVideoData(byte[] videoData) {
        this.videoData = videoData;
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
        buffer.putInt(frameNumber);
        buffer.putInt(frameType);
        buffer.putLong(timestamp);
        buffer.putInt(width);
        buffer.putInt(height);
        buffer.putInt(dataLength);
        buffer.put(videoData, 0, dataLength);
        return buffer.array();
    }

    public static VideoPacket fromBytes(byte[] data) {
        if (data == null || data.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Video packet too small");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        VideoPacket packet = new VideoPacket();
        packet.userId = buffer.getInt();
        packet.frameNumber = buffer.getInt();
        packet.frameType = buffer.getInt();
        packet.timestamp = buffer.getLong();
        packet.width = buffer.getInt();
        packet.height = buffer.getInt();
        packet.dataLength = buffer.getInt();

        if (packet.dataLength > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("Video data too large: " + packet.dataLength);
        }

        if (data.length < HEADER_SIZE + packet.dataLength) {
            throw new IllegalArgumentException("Video packet data incomplete");
        }

        packet.videoData = new byte[packet.dataLength];
        buffer.get(packet.videoData, 0, packet.dataLength);
        return packet;
    }

    public int getSize() {
        return HEADER_SIZE + dataLength;
    }
}
