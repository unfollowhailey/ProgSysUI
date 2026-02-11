package com.securephone.client.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Test UI pour vérifier la lecture des sons MP3
 * Lance une fenêtre avec des boutons pour tester chaque son
 */
public class SoundTestUI {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }
    
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("🔊 Test Sons MP3 - SecurePhone");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Titre
        JLabel titleLabel = new JLabel("🔊 Test des Sons MP3");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Sous-titre
        JLabel subtitleLabel = new JLabel("Cliquez sur un bouton pour tester chaque son");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Récupérer le SoundPlayer
        SoundPlayer player = SoundPlayer.getInstance();
        
        // Boutons pour chaque son
        addSoundButton(mainPanel, "📨 Message Reçu", () -> player.playMessageReceived());
        addSoundButton(mainPanel, "📤 Message Envoyé", () -> player.playMessageSent());
        addSoundButton(mainPanel, "📞 Appel Entrant", () -> player.playCallIncoming());
        addSoundButton(mainPanel, "✅ Appel Connecté", () -> player.playCallConnected());
        addSoundButton(mainPanel, "📴 Appel Terminé", () -> player.playCallEnded());
        addSoundButton(mainPanel, "❌ Erreur", () -> player.playError());
        addSoundButton(mainPanel, "🔘 Clic Bouton", () -> player.playButtonClick());
        addSoundButton(mainPanel, "🔔 Notification", () -> player.playNotification());
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Contrôles
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(new Color(45, 45, 45));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        controlsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Toggle sons
        JCheckBox soundToggle = new JCheckBox("Sons activés", true);
        soundToggle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        soundToggle.setForeground(Color.WHITE);
        soundToggle.setBackground(new Color(45, 45, 45));
        soundToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        soundToggle.addActionListener(e -> {
            player.setSoundEnabled(soundToggle.isSelected());
            System.out.println("Sons " + (soundToggle.isSelected() ? "activés" : "désactivés"));
        });
        controlsPanel.add(soundToggle);
        controlsPanel.add(Box.createVerticalStrut(15));
        
        // Slider volume
        JLabel volumeLabel = new JLabel("Volume: 80%");
        volumeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(volumeLabel);
        controlsPanel.add(Box.createVerticalStrut(5));
        
        JSlider volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setBackground(new Color(45, 45, 45));
        volumeSlider.setForeground(new Color(100, 200, 255));
        volumeSlider.setMaximumSize(new Dimension(400, 40));
        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            float volume = value / 100.0f;
            player.setMasterVolume(volume);
            volumeLabel.setText("Volume: " + value + "%");
        });
        controlsPanel.add(volumeSlider);
        
        mainPanel.add(controlsPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Statut
        JLabel statusLabel = new JLabel("✅ Tous les sons sont prêts");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(76, 175, 80));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private static void addSoundButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(100, 200, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(400, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 180, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 200, 255));
            }
        });
        
        button.addActionListener(e -> {
            System.out.println("Lecture: " + text);
            action.run();
        });
        
        panel.add(button);
        panel.add(Box.createVerticalStrut(10));
    }
}
