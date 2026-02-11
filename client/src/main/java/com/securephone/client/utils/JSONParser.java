package com.securephone.client.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSONParser {

	public static JSONObject parseFile(String filePath) throws IOException {
		String content = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
		return new JSONObject(content);
	}

	public static JSONObject parseFileIfExists(String filePath) {
		try {
			if (!Files.exists(Path.of(filePath))) {
				return new JSONObject();
			}
			return parseFile(filePath);
		} catch (IOException e) {
			return new JSONObject();
		}
	}
}
