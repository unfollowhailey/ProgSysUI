package com.securephone.server.udp;

import com.securephone.shared.protocol.AudioPacket;

public class AudioPacketHandler {

	public AudioPacket parse(byte[] data, int length) {
		if (data == null || length <= 0) {
			return null;
		}
		byte[] buffer = new byte[length];
		System.arraycopy(data, 0, buffer, 0, length);
		return AudioPacket.fromBytes(buffer);
	}
}
