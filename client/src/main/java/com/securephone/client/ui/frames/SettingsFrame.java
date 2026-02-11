package com.securephone.client.ui.frames;

import javax.swing.*;

import java.awt.*;
import com.securephone.client.ui.UIManager;

/**
 * SettingsFrame - Paramètres de l'application
 * 
 * Responsabilités:
 * - Paramètres audio/vidéo
 * - Paramètres de notification
 * - Paramètres de sécurité
 * - Paramètres d'affichage (thème, langue)
 * 
 * Dépendances:
 * - SettingsServlet.java (Hailey) pour persister les settings
 * 
 * @author Hatsu
 */
public class SettingsFrame extends JPanel {
    
    // ========== COMPOSANTS UI ==========
    // Audio
    private JSlider masterVolumeSlider;
    private JCheckBox enableSoundsCheckbox;
    private JCheckBox enableNotificationsCheckbox;
    
    // Vidéo
    private JComboBox<String> resolutionCombo;
    private JComboBox<String> frameRateCombo;
    private JCheckBox mirrorVideoCheckbox;
    
    // Thème
    private JComboBox<String> themeCombo;
    private JComboBox<String> languageCombo;
    
    // Sécurité
    private JCheckBox enable2FACheckbox;
    private JButton changePasswordButton;
    
    // Boutons d'action
    private JButton saveButton;
    private JButton resetButton;
    
    // ========== CONSTRUCTEUR ==========
    public SettingsFrame() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants UI
     */
    private void initComponents() {
        // Audio
        masterVolumeSlider = new JSlider(0, 100, 80);
        masterVolumeSlider.setMajorTickSpacing(20);
        masterVolumeSlider.setPaintTicks(true);
        masterVolumeSlider.setPaintLabels(true);
        
        enableSoundsCheckbox = new JCheckBox("Activer les sons d'interface", true);
        enableNotificationsCheckbox = new JCheckBox("Activer les notifications", true);
        
        // Vidéo
        resolutionCombo = new JComboBox<>(new String[]{"480p", "720p", "1080p"});
        resolutionCombo.setSelectedItem("720p");
        frameRateCombo = new JComboBox<>(new String[]{"24", "30", "60"});
        frameRateCombo.setSelectedItem("30");
        mirrorVideoCheckbox = new JCheckBox("Inverser la vidéo locale", true);
        
        // Thème
        themeCombo = new JComboBox<>(new String[]{"Sombre", "Clair"});
        themeCombo.setSelectedItem("Sombre");
        languageCombo = new JComboBox<>(new String[]{"Français", "English", "Español"});
        languageCombo.setSelectedItem("Français");
        
        // Sécurité
        enable2FACheckbox = new JCheckBox("Activer l'authentification 2FA", true);
        changePasswordButton = new JButton("Changer le mot de passe");
        
        // Boutons d'action
        saveButton = new JButton("Enregistrer");
        resetButton = new JButton("Réinitialiser");
        
        // Style
        UIManager.stylePrimaryButton(saveButton);
        UIManager.styleSecondaryButton(resetButton);
        UIManager.styleSecondaryButton(changePasswordButton);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Section Audio
        JPanel audioPanel = createSection("🔊 Audio");
        audioPanel.add(new JLabel("Volume général:"));
        audioPanel.add(masterVolumeSlider);
        audioPanel.add(enableSoundsCheckbox);
        audioPanel.add(enableNotificationsCheckbox);
        add(audioPanel);
        
        // Séparateur
        add(UIManager.createDivider());
        
        // Section Vidéo
        JPanel videoPanel = createSection("📹 Vidéo");
        JPanel resolutionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resolutionPanel.add(new JLabel("Résolution:"));
        resolutionPanel.add(resolutionCombo);
        videoPanel.add(resolutionPanel);
        
        JPanel frameRatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        frameRatePanel.add(new JLabel("FPS:"));
        frameRatePanel.add(frameRateCombo);
        videoPanel.add(frameRatePanel);
        videoPanel.add(mirrorVideoCheckbox);
        add(videoPanel);
        
        // Séparateur
        add(UIManager.createDivider());
        
        // Section Thème
        JPanel themePanel = createSection("🎨 Affichage");
        JPanel themeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeSelectionPanel.add(new JLabel("Thème:"));
        themeSelectionPanel.add(themeCombo);
        themePanel.add(themeSelectionPanel);
        
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        languagePanel.add(new JLabel("Langue:"));
        languagePanel.add(languageCombo);
        themePanel.add(languagePanel);
        add(themePanel);
        
        // Séparateur
        add(UIManager.createDivider());
        
        // Section Sécurité
        JPanel securityPanel = createSection("🔒 Sécurité");
        securityPanel.add(enable2FACheckbox);
        securityPanel.add(changePasswordButton);
        add(securityPanel);
        
        // Espace flexible
        add(Box.createVerticalGlue());
        
        // Boutons d'action
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(UIManager.getBackground());
        actionPanel.add(resetButton);
        actionPanel.add(saveButton);
        add(actionPanel);
    }
    
    /**
     * Crée une section avec titre
     */
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getSurfaceVariant());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
    }
    
    // ========== GETTERS ==========
    public int getMasterVolume() {
        return masterVolumeSlider.getValue();
    }
    
    public boolean isSoundsEnabled() {
        return enableSoundsCheckbox.isSelected();
    }
    
    public boolean isNotificationsEnabled() {
        return enableNotificationsCheckbox.isSelected();
    }
    
    public String getResolution() {
        return (String) resolutionCombo.getSelectedItem();
    }
    
    public String getFrameRate() {
        return (String) frameRateCombo.getSelectedItem();
    }
    
    public boolean isMirrorVideoEnabled() {
        return mirrorVideoCheckbox.isSelected();
    }
    
    public String getTheme() {
        return (String) themeCombo.getSelectedItem();
    }
    
    public String getLanguage() {
        return (String) languageCombo.getSelectedItem();
    }
    
    public boolean is2FAEnabled() {
        return enable2FACheckbox.isSelected();
    }
    
    // ========== LISTENERS ==========
    public void onSaveClick(Runnable action) {
        saveButton.addActionListener(e -> action.run());
    }
    
    public void onResetClick(Runnable action) {
        resetButton.addActionListener(e -> action.run());
    }
    
    public void onChangePasswordClick(Runnable action) {
        changePasswordButton.addActionListener(e -> action.run());
    }
}
