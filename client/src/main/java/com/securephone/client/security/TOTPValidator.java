package com.securephone.client.security;

public class TOTPValidator {

	public static boolean isValidFormat(String code) {
		if (code == null) {
			return false;
		}
		String trimmed = code.trim();
		return trimmed.matches("\\d{6}");
	}

	public static String normalize(String code) {
		if (code == null) {
			return "";
		}
		return code.replaceAll("\\s+", "").trim();
	}

	public static boolean equals(String code, String expected) {
		String a = normalize(code);
		String b = normalize(expected);
		return !a.isEmpty() && a.equals(b);
	}
}
