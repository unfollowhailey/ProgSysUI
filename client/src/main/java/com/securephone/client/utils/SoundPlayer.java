package com.securephone.client.utils;

import javax.sound.sampled.*;
import javazoom.jl.player.Player;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundPlayer - Gestionnaire de sons de l'interface
 * 
 * Responsabilités:
 * - Charger et jouer les sons d'interface
 * - Gérer le volume global
 * - Cache des sons pour performance
 * 
 * Types de sons:
 * - MESSAGE_RECEIVED: Notification de message reçu
 * - MESSAGE_SENT: Confirmation d'envoi
 * - CALL_INCOMING: Appel entrant
 * - CALL_CONNECTED: Appel connecté
 * - CALL_ENDED: Appel terminé
 * - ERROR: Son d'erreur
 * - BUTTON_CLICK: Clic de bouton
 * 
 * @author Hatsu
 */
public class SoundPlayer {
    
    // ========== CHEMINS DES SONS ==========
    private static final String SOUND_DIR;
    
    static {
        // Déterminer le chemin absolu vers les ressources
        String userDir = System.getProperty("user.dir");
        if (userDir.endsWith("client")) {
            SOUND_DIR = "resources/sounds/";
        } else {
            SOUND_DIR = "client/resources/sounds/";
        }
    }
    
    public enum SoundType {
        MESSAGE_RECEIVED("message_received.mp3"),
        MESSAGE_SENT("message_sent.mp3"),
        CALL_INCOMING("call_incoming.mp3"),
        CALL_CONNECTED("call_connected.mp3"),
        CALL_ENDED("call_ended.mp3"),
        ERROR("error.mp3"),
        BUTTON_CLICK("button_click.mp3"),
        NOTIFICATION("notification.mp3");
        
        private final String filename;
        
        SoundType(String filename) {
            this.filename = filename;
        }
        
        public String getFilename() {
            return filename;
        }
    }
    
    // ========== SINGLETON ==========
    private static SoundPlayer instance;
    
    // ========== VARIABLES D'INSTANCE ==========
    private boolean soundEnabled = true;
    private float masterVolume = 0.8f; // Volume global (0.0 à 1.0)
    private Map<SoundType, Clip> soundCache = new HashMap<>();
    private Player currentPlayer; // Pour lecture MP3 en cours
    
    // ========== CONSTRUCTEUR PRIVÉ ==========
    private SoundPlayer() {
        preloadSounds();
    }
    
    /**
     * Retourne l'instance unique du SoundPlayer
     */
    public static synchronized SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    /**
     * Joue un son
     * @param soundType Type de son à jouer
     */
    public void playSound(SoundType soundType) {
        if (!soundEnabled) {
            return;
        }
        
        // Jouer le son dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                playMP3(soundType);
            } catch (Exception e) {
                System.err.println("Erreur lors de la lecture du son " + soundType + ": " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Joue un fichier MP3
     */
    private void playMP3(SoundType soundType) {
        try {
            String filepath = SOUND_DIR + soundType.getFilename();
            File soundFile = new File(filepath);
            
            if (!soundFile.exists()) {
                System.err.println("Fichier son introuvable: " + filepath);
                return;
            }
            
            FileInputStream fis = new FileInputStream(soundFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            // Arrêter le son précédent si en cours
            if (currentPlayer != null) {
                currentPlayer.close();
            }
            
            currentPlayer = new Player(bis);
            currentPlayer.play();
            
            // Nettoyer les ressources
            currentPlayer.close();
            bis.close();
            fis.close();
            
        } catch (Exception e) {
            System.err.println("Erreur lecture MP3: " + e.getMessage());
        }
    }
    
    /**
     * Active ou désactive les sons
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }
    
    /**
     * Vérifie si les sons sont activés
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Définit le volume global (0.0 à 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Retourne le volume global
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Arrête tous les sons en cours
     */
    public void stopAllSounds() {
        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
    }
    
    /**
     * Libère toutes les ressources audio
     */
    public void dispose() {
        stopAllSounds();
        soundCache.clear();
    }
    
    // ========== MÉTHODES PRIVÉES ==========
    
    /**
     * Précharge tous les sons en mémoire (vérifie leur présence)
     */
    private void preloadSounds() {
        System.out.println("Vérification des fichiers sons...");
        for (SoundType type : SoundType.values()) {
            String filepath = SOUND_DIR + type.getFilename();
            File soundFile = new File(filepath);
            if (soundFile.exists()) {
                System.out.println("  ✓ " + type.name() + " trouvé");
            } else {
                System.out.println("  ✗ " + type.name() + " introuvable: " + filepath);
            }
        }
    }
    
    // Note: loadSound et setClipVolume ne sont plus utilisés avec MP3
    // Le contrôle du volume pour MP3 nécessiterait une bibliothèque supplémentaire
    
    // ========== MÉTHODES DE COMMODITÉ ==========
    
    /**
     * Joue le son de message reçu
     */
    public void playMessageReceived() {
        playSound(SoundType.MESSAGE_RECEIVED);
    }
    
    /**
     * Joue le son de message envoyé
     */
    public void playMessageSent() {
        playSound(SoundType.MESSAGE_SENT);
    }
    
    /**
     * Joue le son d'appel entrant
     */
    public void playCallIncoming() {
        playSound(SoundType.CALL_INCOMING);
    }
    
    /**
     * Joue le son d'appel connecté
     */
    public void playCallConnected() {
        playSound(SoundType.CALL_CONNECTED);
    }
    
    /**
     * Joue le son d'appel terminé
     */
    public void playCallEnded() {
        playSound(SoundType.CALL_ENDED);
    }
    
    /**
     * Joue le son d'erreur
     */
    public void playError() {
        playSound(SoundType.ERROR);
    }
    
    /**
     * Joue le son de clic de bouton
     */
    public void playButtonClick() {
        playSound(SoundType.BUTTON_CLICK);
    }
    
    /**
     * Joue le son de notification générique
     */
    public void playNotification() {
        playSound(SoundType.NOTIFICATION);
    }
}