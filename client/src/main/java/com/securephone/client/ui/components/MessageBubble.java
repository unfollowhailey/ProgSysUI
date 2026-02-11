package com.securephone.client.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.securephone.client.ui.UIManager;

/**
 * MessageBubble - Composant pour afficher un message de chat
 * 
 * Responsabilités:
 * - Afficher le texte du message
 * - Afficher l'avatar/nom de l'expéditeur
 * - Afficher le timestamp
 * - Styling différencié (envoyé vs reçu)
 * - Support des emojis et formatage basique
 * 
 * Dépendances:
 * - ChatMessage.java (modèle)
 * 
 * @author Hatsu
 */
public class MessageBubble extends JPanel {
    
    // ========== VARIABLES ==========
    private String senderName;
    private String messageContent;
    private String timestamp;
    private boolean isSentByMe;
    
    // ========== COMPOSANTS ==========
    private JLabel avatarLabel;
    private JLabel senderLabel;
    private JTextArea messageTextArea;
    private JLabel timeLabel;
    
    // ========== CONSTRUCTEUR ==========
    public MessageBubble(String senderName, String messageContent, String timestamp, boolean isSentByMe) {
        this.senderName = senderName;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
        
        initComponents();
        setupLayout();
        setupStyle();
    }
    
    /**
     * Initialise les composants
     */
    private void initComponents() {
        // Avatar (cercle avec initiales)
        avatarLabel = new JLabel(getInitials(senderName));
        avatarLabel.setFont(UIManager.Fonts.BODY_BOLD);
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setOpaque(true);
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        
        // Nom de l'expéditeur
        senderLabel = new JLabel(senderName);
        UIManager.styleCaption(senderLabel);
        
        // Contenu du message
        messageTextArea = new JTextArea(messageContent);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setOpaque(false);
        messageTextArea.setFont(UIManager.Fonts.BODY);
        
        // Timestamp
        timeLabel = new JLabel(timestamp);
        UIManager.styleCaption(timeLabel);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        if (isSentByMe) {
            // Message envoyé: aligné à droite
            JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
            rightPanel.setOpaque(false);
            
            JPanel textPanel = new JPanel(new BorderLayout(5, 2));
            textPanel.setOpaque(false);
            textPanel.add(senderLabel, BorderLayout.NORTH);
            textPanel.add(messageTextArea, BorderLayout.CENTER);
            textPanel.add(timeLabel, BorderLayout.SOUTH);
            
            rightPanel.add(textPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        } else {
            // Message reçu: aligné à gauche
            JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
            leftPanel.setOpaque(false);
            
            leftPanel.add(avatarLabel, BorderLayout.WEST);
            
            JPanel textPanel = new JPanel(new BorderLayout(5, 2));
            textPanel.setOpaque(false);
            textPanel.add(senderLabel, BorderLayout.NORTH);
            textPanel.add(messageTextArea, BorderLayout.CENTER);
            textPanel.add(timeLabel, BorderLayout.SOUTH);
            
            leftPanel.add(textPanel, BorderLayout.CENTER);
            add(leftPanel, BorderLayout.WEST);
        }
    }
    
    /**
     * Applique le style
     */
    private void setupStyle() {
        if (isSentByMe) {
            // Message envoyé: bleu clair
            setBackground(UIManager.getPrimary());
            messageTextArea.setForeground(Color.WHITE);
        } else {
            // Message reçu: surface alternative
            setBackground(UIManager.getSurfaceVariant());
            messageTextArea.setForeground(UIManager.getTextPrimary());
            
            // Avatar: couleur primaire
            avatarLabel.setBackground(UIManager.getPrimary());
            avatarLabel.setForeground(Color.WHITE);
        }
        setOpaque(true);
    }
    
    /**
     * Récupère les initiales du nom
     */
    private String getInitials(String name) {
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
    
    // ========== GETTERS ==========
    public String getSenderName() {
        return senderName;
    }
    
    public String getMessageContent() {
        return messageContent;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public boolean isSentByMe() {
        return isSentByMe;
    }
}
