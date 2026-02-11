package com.securephone.client.models;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

public class AudioDevice {

	public enum Type {
		INPUT,
		OUTPUT
	}

	private final String name;
	private final String description;
	private final Type type;
	private final Mixer.Info mixerInfo;

	public AudioDevice(String name, String description, Type type, Mixer.Info mixerInfo) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.mixerInfo = mixerInfo;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Type getType() {
		return type;
	}

	public Mixer.Info getMixerInfo() {
		return mixerInfo;
	}

	public static List<AudioDevice> listInputDevices() {
		return listDevices(Type.INPUT);
	}

	public static List<AudioDevice> listOutputDevices() {
		return listDevices(Type.OUTPUT);
	}

	private static List<AudioDevice> listDevices(Type type) {
		List<AudioDevice> devices = new ArrayList<>();
		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info lineInfo = type == Type.INPUT
				? new Line.Info(TargetDataLine.class)
				: new Line.Info(SourceDataLine.class);

			if (mixer.isLineSupported(lineInfo)) {
				devices.add(new AudioDevice(info.getName(), info.getDescription(), type, info));
			}
		}
		return devices;
	}

	@Override
	public String toString() {
		return String.format("AudioDevice{name='%s', type=%s}", name, type);
	}
}
