package com.securephone.client.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	public enum Level {
		DEBUG,
		INFO,
		WARN,
		ERROR
	}

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static Level currentLevel = Level.INFO;

	public static void setLevel(Level level) {
		currentLevel = level == null ? Level.INFO : level;
	}

	public static void debug(String message) {
		log(Level.DEBUG, message, null);
	}

	public static void info(String message) {
		log(Level.INFO, message, null);
	}

	public static void warn(String message) {
		log(Level.WARN, message, null);
	}

	public static void error(String message) {
		log(Level.ERROR, message, null);
	}

	public static void error(String message, Throwable throwable) {
		log(Level.ERROR, message, throwable);
	}

	private static void log(Level level, String message, Throwable throwable) {
		if (level.ordinal() < currentLevel.ordinal()) {
			return;
		}

		String timestamp = LocalDateTime.now().format(FORMATTER);
		String line = String.format("%s [%s] %s", timestamp, level.name(), message);

		if (level == Level.ERROR) {
			System.err.println(line);
			if (throwable != null) {
				throwable.printStackTrace(System.err);
			}
		} else {
			System.out.println(line);
		}
	}
}
