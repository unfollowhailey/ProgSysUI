package com.securephone.client.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class ApiClient {

	private final String baseUrl;

	public ApiClient() {
		Properties props = new Properties();
		String host = "localhost";
		String port = "8080";
		try (var fis = new java.io.FileInputStream("client/resources/config.properties")) {
			props.load(fis);
			host = props.getProperty("server.host", host);
			port = props.getProperty("server.port", port);
		} catch (Exception ignored) {
		}
		this.baseUrl = "http://" + host + ":" + port + "/securephone/api";
	}

	public JSONObject post(String path, Map<String, String> params) throws Exception {
		URL url = new URL(baseUrl + path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String body = buildQuery(params);
		try (OutputStream os = conn.getOutputStream()) {
			os.write(body.getBytes(StandardCharsets.UTF_8));
		}

		return readResponse(conn);
	}

	public JSONObject get(String path, Map<String, String> params) throws Exception {
		String query = buildQuery(params);
		URL url = new URL(baseUrl + path + (query.isEmpty() ? "" : "?" + query));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		return readResponse(conn);
	}

	private JSONObject readResponse(HttpURLConnection conn) throws Exception {
		int code = conn.getResponseCode();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
			code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
			StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		return new JSONObject(sb.toString());
	}

	private String buildQuery(Map<String, String> params) throws Exception {
		if (params == null || params.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (sb.length() > 0) sb.append("&");
			sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
		}
		return sb.toString();
	}
}
