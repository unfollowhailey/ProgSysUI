package com.securephone.client.video;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class VideoPlayer extends JPanel {

    private BufferedImage currentFrame;

    public VideoPlayer() {
        setBackground(Color.BLACK);
    }

    public void setFrame(BufferedImage frame) {
        this.currentFrame = frame;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentFrame == null) {
            return;
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = currentFrame.getWidth();
        int imgHeight = currentFrame.getHeight();

        double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
        int drawWidth = (int) (imgWidth * scale);
        int drawHeight = (int) (imgHeight * scale);
        int x = (panelWidth - drawWidth) / 2;
        int y = (panelHeight - drawHeight) / 2;

        g.drawImage(currentFrame, x, y, drawWidth, drawHeight, null);
    }
}
