package com.securephone.client.ui.frames;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.securephone.client.ui.UIManager;
import com.securephone.client.models.ChatMessage;
import com.securephone.client.ui.components.PTTButton;

/**
 * ChatFrame - Interface de chat principal
 * 
 * Responsabilités:
 * - Afficher les messages avec MessageBubble
 * - Gestion champ d'entrée et envoi
 * - Controles audio/vidéo (PTTButton, AudioControls)
 * - Intégration WebSocket pour messages en temps réel
 * 
 * Dépendances:
 * - WebSocketClient.java (Hailey) pour messages
 * - ChatMessage.java (modèle partagé)
 * - MessageBubble.java, PTTButton.java, AudioControls.java (composants Hatsu)
 * - Protocole Tflow pour audio/vidéo
 * 
 * @author Hatsu
 */
public class ChatFrame extends JPanel {
    
    // ========== COMPOSANTS UI ==========
    private JTextArea messageAreaDisplay;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton emojiButton;
    private JButton attachButton;
    
    // Audio/Video controls
    private PTTButton pttButton; // Push-To-Talk
    private JButton audioToggleButton;
    private JButton videoToggleButton;
    
    private JLabel statusLabel;
    private JLabel contactLabel;
    private String activeContactName;
    
    // ========== VARIABLES ==========
    private List<ChatMessage> messages = new ArrayList<>();
    
    // ========== CONSTRUCTEUR ==========
    public ChatFrame() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants UI
     */
    private void initComponents() {
        // Affichage des messages (Zone lecture-seule)
        messageAreaDisplay = new JTextArea();
        messageAreaDisplay.setEditable(false);
        messageAreaDisplay.setLineWrap(true);
        messageAreaDisplay.setWrapStyleWord(true);
        messageAreaDisplay.setFont(UIManager.Fonts.BODY);
        
        // Champ d'entrée
        messageInputField = new JTextField();
        UIManager.styleTextField(messageInputField);
        
        // Boutons d'action
        sendButton = new JButton("Envoyer");
        emojiButton = new JButton("😀");
        attachButton = new JButton("📎");
        
        // Boutons audio/vidéo
        pttButton = new PTTButton();
        audioToggleButton = new JButton("🔊 Audio");
        videoToggleButton = new JButton("📹 Vidéo");
        
        // Status
        statusLabel = new JLabel("En ligne");
        
        // Style
        UIManager.stylePrimaryButton(sendButton);
        // PTTButton gère son propre style
        UIManager.styleSecondaryButton(audioToggleButton);
        UIManager.styleSecondaryButton(videoToggleButton);
    }
    
    /**
     * Configure le layout (BorderLayout + GridBag)
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Haut: Sélection du contact + status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getSurface());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getBorder()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        contactLabel = new JLabel("Contact: [À sélectionner]");
        UIManager.styleBody(contactLabel);
        topPanel.add(contactLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Centre: Zone des messages
        JScrollPane scrollPane = new JScrollPane(messageAreaDisplay);
        scrollPane.setBackground(UIManager.getSurface());
        add(scrollPane, BorderLayout.CENTER);
        
        // Bas: Input + Controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIManager.getBackground());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Zone input
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(UIManager.getBackground());
        inputPanel.add(messageInputField, BorderLayout.CENTER);
        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(attachButton, BorderLayout.EAST);
        
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Zone boutons send et controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(UIManager.getBackground());
        controlPanel.add(pttButton);
        controlPanel.add(audioToggleButton);
        controlPanel.add(videoToggleButton);
        controlPanel.add(sendButton);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UIManager.getBackground());
        southPanel.add(bottomPanel, BorderLayout.CENTER);
        southPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(southPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
        messageAreaDisplay.setBackground(UIManager.getSurface());
        messageAreaDisplay.setForeground(UIManager.getTextPrimary());
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    /**
     * Ajoute un message à l'affichage
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        String displayText = String.format("[%s] %s: %s\n", 
            message.getTimestamp(), 
            message.getSender(), 
            message.getContent());
        messageAreaDisplay.append(displayText);
        messageAreaDisplay.setCaretPosition(messageAreaDisplay.getDocument().getLength());
    }
    
    /**
     * Affiche un message d'erreur
     */
    public void showError(String error) {
        statusLabel.setText("❌ " + error);
        statusLabel.setForeground(UIManager.getError());
    }
    
    /**
     * Met à jour le statut
     */
    public void setStatus(String status) {
        statusLabel.setText("🟢 " + status);
        statusLabel.setForeground(UIManager.getSuccess());
    }
    
    /**
     * Récupère le texte entré
     */
    public String getInputText() {
        return messageInputField.getText();
    }
    
    /**
     * Efface le champ d'entrée
     */
    public void clearInput() {
        messageInputField.setText("");
    }
    
    /**
     * Efface l'historique des messages
     */
    public void clearMessages() {
        messageAreaDisplay.setText("");
        messages.clear();
    }

    public void setActiveContact(String contactName) {
        activeContactName = contactName;
        if (contactName == null || contactName.isEmpty()) {
            contactLabel.setText("Contact: [À sélectionner]");
        } else {
            contactLabel.setText("Contact: " + contactName);
        }
    }

    public String getActiveContact() {
        return activeContactName;
    }
    
    // ========== LISTENERS ==========
    public void onSendButtonClick(Runnable action) {
        sendButton.addActionListener(e -> action.run());
    }
    
    public void onPTTButtonClick(Runnable action) {
        pttButton.addActionListener(e -> action.run());
    }

    public void onPTTStart(Runnable action) {
        pttButton.setOnRecordingStarted(action);
    }

    public void onPTTStop(java.util.function.Consumer<Long> action) {
        pttButton.setOnRecordingStopped(action);
    }
    
    public void onAudioToggleClick(Runnable action) {
        audioToggleButton.addActionListener(e -> action.run());
    }
    
    public void onVideoToggleClick(Runnable action) {
        videoToggleButton.addActionListener(e -> action.run());
    }
}
