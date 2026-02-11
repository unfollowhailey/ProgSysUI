package com.securephone.client.ui.frames;

import javax.swing.*;
import java.awt.*;
import com.securephone.client.ui.UIManager;

/**
 * LoginFrame - Écran de connexion utilisateur
 * 
 * Responsabilités:
 * - Afficher les champs de connexion (username/password/2FA)
 * - Gérer les événements de connexion
 * - Communiquer avec AuthServlet pour l'authentification
 * 
 * Dépendances:
 * - ApiClient.java (Hailey) pour appels AUTH
 * - AuthServlet.java (Hailey) pour endpoint /auth/login
 * 
 * @author Hatsu
 */
public class LoginFrame extends JPanel {
    
    // ========== COMPOSANTS UI ==========
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField totpField; // 2FA
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private JCheckBox rememberMeCheckbox;
    
    // ========== CONSTRUCTEUR ==========
    public LoginFrame() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants UI
     */
    private void initComponents() {
        // Titre
        JLabel titleLabel = new JLabel("SecurePhone");
        
        // Champs de saisie
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        totpField = new JTextField(6);
        
        // Boutons
        loginButton = new JButton("Connexion");
        registerButton = new JButton("S'inscrire");
        rememberMeCheckbox = new JCheckBox("Se souvenir de moi");
        
        // Erreurs
        errorLabel = new JLabel();
        errorLabel.setForeground(UIManager.getError());
        
        // Style
        UIManager.styleTitle(titleLabel);
        UIManager.stylePrimaryButton(loginButton);
        UIManager.styleSecondaryButton(registerButton);
        UIManager.styleTextField(usernameField);
        UIManager.stylePasswordField(passwordField);
        UIManager.styleTextField(totpField);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("SecurePhone");
        UIManager.styleTitle(titleLabel);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Nom d'utilisateur:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);
        
        // 2FA
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Code 2FA:"), gbc);
        gbc.gridx = 1;
        add(totpField, gbc);
        
        // Remember me
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(rememberMeCheckbox, gbc);
        
        // Error label
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(errorLabel, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        add(loginButton, gbc);
        gbc.gridx = 1;
        add(registerButton, gbc);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
    }
    
    // ========== GETTERS ==========
    public String getUsername() {
        return usernameField.getText();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    public String getTOTPCode() {
        return totpField.getText();
    }
    
    public boolean isRememberMeChecked() {
        return rememberMeCheckbox.isSelected();
    }
    
    // ========== SETTERS ==========
    public void setError(String message) {
        errorLabel.setText(message);
    }
    
    public void clearError() {
        errorLabel.setText("");
    }
    
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        totpField.setText("");
    }

    public void setTOTPCode(String code) {
        totpField.setText(code == null ? "" : code);
        totpField.requestFocusInWindow();
        totpField.selectAll();
    }
    
    // ========== LISTENERS ==========
    public void onLoginButtonClick(Runnable action) {
        loginButton.addActionListener(e -> action.run());
    }
    
    public void onRegisterButtonClick(Runnable action) {
        registerButton.addActionListener(e -> action.run());
    }
}
