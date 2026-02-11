package com.securephone.client.audio;

/**
 * Placeholder Opus codec (pass-through).
 * Replace with a real Opus binding for production-quality audio.
 */
public class OpusCodec {

	public byte[] encode(byte[] pcm) {
		if (pcm == null) {
			return new byte[0];
		}
		return pcm;
	}

	public byte[] decode(byte[] encoded) {
		if (encoded == null) {
			return new byte[0];
		}
		return encoded;
	}
}
