package com.securephone.client.ui.components;

import javax.swing.*;
import java.awt.*;
import com.securephone.client.ui.UIManager;

/**
 * AudioControls - Composant pour contrôler l'audio et la vidéo
 * 
 * Responsabilités:
 * - Contrôles de volume (slider)
 * - Toggle audio (mute/unmute)
 * - Toggle vidéo (caméra on/off)
 * - Affichage de la qualité de connexion
 * - Feedback visuel sur l'état des périphériques
 * 
 * Dépendances:
 * - AudioClient.java (Tflow)
 * - VideoClient.java (Tflow)
 * 
 * @author Hatsu
 */
public class AudioControls extends JPanel {
    
    // ========== COMPOSANTS ==========
    private JSlider volumeSlider;
    private JButton muteButton;
    private JButton cameraButton;
    private JLabel signalQualityLabel;
    private JLabel volumeLabel;
    
    // ========== VARIABLES ==========
    private boolean isMuted = false;
    private boolean isCameraOn = false;
    private int currentVolume = 80;
    
    // ========== CONSTRUCTEUR ==========
    public AudioControls() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants
     */
    private void initComponents() {
        // Slider de volume
        volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.addChangeListener(e -> {
            currentVolume = volumeSlider.getValue();
            updateVolumeLabel();
            onVolumeChanged(currentVolume);
        });
        
        // Bouton mute/unmute
        muteButton = new JButton("🔊 Audio");
        muteButton.setFont(UIManager.Fonts.BODY_BOLD);
        muteButton.addActionListener(e -> toggleMute());
        
        // Bouton caméra
        cameraButton = new JButton("📹 Caméra OFF");
        cameraButton.setFont(UIManager.Fonts.BODY_BOLD);
        cameraButton.addActionListener(e -> toggleCamera());
        
        // Label volume
        volumeLabel = new JLabel("80%");
        UIManager.styleCaption(volumeLabel);
        
        // Label qualité du signal
        signalQualityLabel = new JLabel("🟢 Signal bon");
        UIManager.styleCaption(signalQualityLabel);
        
        // Style des boutons
        UIManager.styleSecondaryButton(muteButton);
        UIManager.styleSecondaryButton(cameraButton);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Ligne 1: Volume
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Volume:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(volumeSlider, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        add(volumeLabel, gbc);
        
        // Ligne 2: Boutons mute/caméra
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(muteButton, gbc);
        
        gbc.gridx = 1;
        add(cameraButton, gbc);
        
        // Ligne 3: Signal qualité
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        add(signalQualityLabel, gbc);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getBorder()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }
    
    // ========== MÉTHODES DE CONTRÔLE ==========
    
    /**
     * Active/désactive le micro
     */
    private void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            muteButton.setText("🔇 Audio OFF");
            onMute();
        } else {
            muteButton.setText("🔊 Audio");
            onUnmute();
        }
    }
    
    /**
     * Active/désactive la caméra
     */
    private void toggleCamera() {
        isCameraOn = !isCameraOn;
        if (isCameraOn) {
            cameraButton.setText("📹 Caméra ON");
            cameraButton.setForeground(UIManager.getSuccess());
            onCameraOn();
        } else {
            cameraButton.setText("📹 Caméra OFF");
            cameraButton.setForeground(UIManager.getTextPrimary());
            onCameraOff();
        }
    }
    
    /**
     * Met à jour le label du volume
     */
    private void updateVolumeLabel() {
        volumeLabel.setText(currentVolume + "%");
    }
    
    /**
     * Met à jour l'indicateur de qualité du signal
     */
    public void setSignalQuality(String quality) {
        switch (quality.toLowerCase()) {
            case "excellent":
                signalQualityLabel.setText("🟢 Excellent");
                signalQualityLabel.setForeground(UIManager.getSuccess());
                break;
            case "good":
                signalQualityLabel.setText("🟢 Bon");
                signalQualityLabel.setForeground(UIManager.getSuccess());
                break;
            case "fair":
                signalQualityLabel.setText("🟡 Moyen");
                signalQualityLabel.setForeground(UIManager.getWarning());
                break;
            case "poor":
                signalQualityLabel.setText("🔴 Mauvais");
                signalQualityLabel.setForeground(UIManager.getError());
                break;
        }
    }
    
    // ========== GETTERS ==========
    
    public int getVolume() {
        return currentVolume;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public boolean isCameraOn() {
        return isCameraOn;
    }
    
    // ========== CALLBACKS ==========
    
    /**
     * Appelé quand le volume change
     */
    protected void onVolumeChanged(int volume) {
        System.out.println("[AudioControls] Volume: " + volume + "%");
    }
    
    /**
     * Appelé quand le micro est désactivé
     */
    protected void onMute() {
        System.out.println("[AudioControls] Micro désactivé");
    }
    
    /**
     * Appelé quand le micro est activé
     */
    protected void onUnmute() {
        System.out.println("[AudioControls] Micro activé");
    }
    
    /**
     * Appelé quand la caméra est activée
     */
    protected void onCameraOn() {
        System.out.println("[AudioControls] Caméra activée");
    }
    
    /**
     * Appelé quand la caméra est désactivée
     */
    protected void onCameraOff() {
        System.out.println("[AudioControls] Caméra désactivée");
    }
    
    // ========== SETTERS POUR LISTENERS ==========
    
    public void setOnVolumeChanged(java.util.function.IntConsumer listener) {
        volumeSlider.addChangeListener(e -> listener.accept(volumeSlider.getValue()));
    }
    
    public void setOnMute(Runnable action) {
        muteButton.addActionListener(e -> {
            if (isMuted) action.run();
        });
    }
    
    public void setOnUnmute(Runnable action) {
        muteButton.addActionListener(e -> {
            if (!isMuted) action.run();
        });
    }
    
    public void setOnCameraOn(Runnable action) {
        cameraButton.addActionListener(e -> {
            if (isCameraOn) action.run();
        });
    }
    
    public void setOnCameraOff(Runnable action) {
        cameraButton.addActionListener(e -> {
            if (!isCameraOn) action.run();
        });
    }
}
