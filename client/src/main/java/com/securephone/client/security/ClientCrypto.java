package com.securephone.client.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class ClientCrypto {

	private static final String AES_ALGO = "AES";
	private static final String AES_GCM = "AES/GCM/NoPadding";
	private static final int GCM_TAG_BITS = 128;
	private static final int IV_BYTES = 12;
	private static final int KEY_BITS = 128;
	private static final int PBKDF2_ITERS = 65536;

	private static final SecureRandom RANDOM = new SecureRandom();

	public static SecretKey generateRandomKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance(AES_ALGO);
		generator.init(KEY_BITS, RANDOM);
		return generator.generateKey();
	}

	public static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERS, KEY_BITS);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] keyBytes = factory.generateSecret(spec).getEncoded();
		return new SecretKeySpec(keyBytes, AES_ALGO);
	}

	public static byte[] randomSalt(int length) {
		byte[] salt = new byte[length];
		RANDOM.nextBytes(salt);
		return salt;
	}

	public static String encryptToBase64(String plaintext, SecretKey key) throws Exception {
		byte[] iv = new byte[IV_BYTES];
		RANDOM.nextBytes(iv);

		Cipher cipher = Cipher.getInstance(AES_GCM);
		cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
		byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

		ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
		buffer.put(iv);
		buffer.put(ciphertext);
		return Base64.getEncoder().encodeToString(buffer.array());
	}

	public static String decryptFromBase64(String base64, SecretKey key) throws Exception {
		byte[] payload = Base64.getDecoder().decode(base64);
		if (payload.length < IV_BYTES) {
			throw new IllegalArgumentException("Payload trop court");
		}
		byte[] iv = new byte[IV_BYTES];
		byte[] ciphertext = new byte[payload.length - IV_BYTES];
		System.arraycopy(payload, 0, iv, 0, IV_BYTES);
		System.arraycopy(payload, IV_BYTES, ciphertext, 0, ciphertext.length);

		Cipher cipher = Cipher.getInstance(AES_GCM);
		cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
		byte[] plaintext = cipher.doFinal(ciphertext);
		return new String(plaintext, StandardCharsets.UTF_8);
	}
}
