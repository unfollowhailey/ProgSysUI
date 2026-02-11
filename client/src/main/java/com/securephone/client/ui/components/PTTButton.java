package com.securephone.client.ui.components;

import javax.swing.*;
import java.awt.*;
import com.securephone.client.ui.UIManager;

/**
 * PTTButton - Bouton Push-To-Talk pour l'audio
 * 
 * Responsabilités:
 * - Gérer l'appui et le relâchement pour l'enregistrement audio
 * - Afficher l'état de l'enregistrement (en cours / arrêté)
 * - Feedback visuel (changement de couleur, animation)
 * - Intégration avec AudioClient
 * 
 * Interaction:
 * - Appui: commence l'enregistrement audio
 * - Relâchement: arrête et envoie l'audio
 * 
 * Dépendances:
 * - AudioClient.java (Tflow) pour enregistrement/envoi
 * 
 * @author Hatsu
 */
public class PTTButton extends JButton {
    
    // ========== VARIABLES ==========
    private boolean isRecording = false;
    private long recordingStartTime;
    
    // ========== CONSTANTES ==========
    private static final int BUTTON_SIZE = 60;
    private static final Color IDLE_COLOR = UIManager.getPrimary();
    private static final Color RECORDING_COLOR = UIManager.getError();
    
    // ========== CONSTRUCTEUR ==========
    public PTTButton() {
        super("🎤");
        initButton();
        setupListeners();
    }
    
    /**
     * Initialise les propriétés du bouton
     */
    private void initButton() {
        setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setFont(new Font("Arial", Font.BOLD, 20));
        setBackground(IDLE_COLOR);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Configure les listeners pour les interactions
     */
    private void setupListeners() {
        // Souris appuyée: commence l'enregistrement
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                startRecording();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                stopRecording();
            }
        });
        
        // Touch (mobile si applicable)
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isRecording) {
                    setBackground(UIManager.getPrimary());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!isRecording) {
                    setBackground(IDLE_COLOR);
                }
            }
        });
    }
    
    /**
     * Commence l'enregistrement audio
     */
    private void startRecording() {
        if (!isRecording) {
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            
            // Feedback visuel
            setBackground(RECORDING_COLOR);
            setText("🎤 REC");
            setToolTipText("Enregistrement en cours... Relâchez pour envoyer");
            
            // Callback
            onRecordingStarted();
        }
    }
    
    /**
     * Arrête l'enregistrement audio
     */
    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            long recordingDuration = System.currentTimeMillis() - recordingStartTime;
            
            // Feedback visuel
            setBackground(IDLE_COLOR);
            setText("🎤");
            setToolTipText("Maintenez pour enregistrer un message audio");
            
            // Callback
            onRecordingStopped(recordingDuration);
        }
    }
    
    /**
     * Retourne l'état d'enregistrement
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    // ========== CALLBACKS ==========
    
    /**
     * Appelé quand l'enregistrement commence
     * À surcharger pour intégrer AudioClient
     */
    protected void onRecordingStarted() {
        System.out.println("[PTTButton] Enregistrement démarré");
    }
    
    /**
     * Appelé quand l'enregistrement s'arrête
     * À surcharger pour intégrer AudioClient
     * 
     * @param duration Durée de l'enregistrement en ms
     */
    protected void onRecordingStopped(long duration) {
        System.out.println("[PTTButton] Enregistrement arrêté après " + duration + "ms");
    }
    
    // ========== SETTERS POUR CALLBACKS ==========
    
    /**
     * Définit l'action quand l'enregistrement commence
     */
    public void setOnRecordingStarted(Runnable action) {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
    }
    
    /**
     * Définit l'action quand l'enregistrement s'arrête
     */
    public void setOnRecordingStopped(java.util.function.Consumer<Long> action) {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                long duration = System.currentTimeMillis() - recordingStartTime;
                action.accept(duration);
            }
        });
    }
}
