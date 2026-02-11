package com.securephone.server.udp;

import com.securephone.shared.protocol.VideoPacket;

public class VideoPacketHandler {

	public VideoPacket parse(byte[] data, int length) {
		if (data == null || length <= 0) {
			return null;
		}
		byte[] buffer = new byte[length];
		System.arraycopy(data, 0, buffer, 0, length);
		return VideoPacket.fromBytes(buffer);
	}
}
