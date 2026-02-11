package com.securephone.client.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

    private static final AudioFormat DEFAULT_FORMAT =
        new AudioFormat(48000f, 16, 1, true, false);

    private SourceDataLine line;
    private boolean started;
    private final AudioBuffer buffer;
    private Thread playThread;
    private volatile boolean running;

    public AudioPlayer() {
        this.buffer = null;
    }

    public AudioPlayer(AudioBuffer buffer) {
        this.buffer = buffer;
    }

    public void start() throws Exception {
        if (buffer == null) {
            throw new IllegalStateException("AudioBuffer requis pour start()");
        }
        start(DEFAULT_FORMAT);
        startBufferLoop();
    }

    public void start(AudioFormat format) throws Exception {
        if (started) {
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        started = true;
    }

    private void startBufferLoop() {
        if (running) {
            return;
        }
        running = true;
        playThread = new Thread(() -> {
            while (running) {
                try {
                    byte[] frame = buffer.poll(100);
                    if (frame != null) {
                        play(frame, frame.length);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }, "AudioPlayerThread");
        playThread.setDaemon(true);
        playThread.start();
    }

    public void play(byte[] data, int length) {
        if (!started || line == null || data == null || length <= 0) {
            return;
        }
        line.write(data, 0, length);
    }

    public void stop() {
        running = false;
        if (playThread != null) {
            try {
                playThread.join(200);
            } catch (InterruptedException ignored) {
            }
            playThread = null;
        }
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
            line = null;
        }
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
