package com.securephone.client.security;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyExchange {

	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(256);
		return generator.generateKeyPair();
	}

	public static String publicKeyToBase64(PublicKey publicKey) {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	public static PublicKey publicKeyFromBase64(String base64) throws Exception {
		byte[] encoded = Base64.getDecoder().decode(base64);
		KeyFactory factory = KeyFactory.getInstance("EC");
		return factory.generatePublic(new X509EncodedKeySpec(encoded));
	}

	public static byte[] deriveSharedSecret(PrivateKey privateKey, PublicKey otherPublic) throws Exception {
		KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
		agreement.init(privateKey);
		agreement.doPhase(otherPublic, true);
		return agreement.generateSecret();
	}

	public static SecretKey deriveAesKey(byte[] sharedSecret) {
		byte[] keyBytes = new byte[16];
		System.arraycopy(sharedSecret, 0, keyBytes, 0, Math.min(sharedSecret.length, 16));
		return new SecretKeySpec(keyBytes, "AES");
	}
}
