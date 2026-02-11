package com.securephone.client.ui.frames;

import javax.swing.*;
import com.securephone.client.ui.UIManager;

/**
 * MainFrame - Fenêtre principale de l'application
 * 
 * Responsabilités:
 * - Conteneur principal avec menu et tabs
 * - Navigation entre Login, Chat, Contacts, Settings
 * - Gestion du cycle de vie de l'application
 * - Intégration des sous-frames
 * 
 * Structure:
 * - Menu bar (Fichier, Édition, Affichage, Aide)
 * - Tab pane: LoginFrame, ChatFrame, ContactFrame, SettingsFrame
 * 
 * @author Hatsu
 */
public class MainFrame extends JFrame {
    
    // ========== COMPOSANTS PRINCIPAUX ==========
    private JTabbedPane tabbedPane;
    private LoginFrame loginFrame;
    private ChatFrame chatFrame;
    private ContactFrame contactFrame;
    private SettingsFrame settingsFrame;
    
    // Menu
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;

    private JMenuItem logoutItem;
    
    // ========== CONSTRUCTEUR ==========
    public MainFrame() {
        initFrame();
        initComponents();
        setupLayout();
        setupTheme();
        setupMenuBar();
    }
    
    /**
     * Initialise les propriétés de la fenêtre
     */
    private void initFrame() {
        setTitle("SecurePhone - Messagerie Sécurisée");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    /**
     * Initialise les composants
     */
    private void initComponents() {
        // Créer les sous-frames
        loginFrame = new LoginFrame();
        chatFrame = new ChatFrame();
        contactFrame = new ContactFrame();
        settingsFrame = new SettingsFrame();
        
        // Tab pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Connexion", loginFrame);
        tabbedPane.addTab("Chat", chatFrame);
        tabbedPane.addTab("Contacts", contactFrame);
        tabbedPane.addTab("Paramètres", settingsFrame);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setContentPane(tabbedPane);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.styleFrame(this);
        tabbedPane.setBackground(UIManager.getBackground());
        tabbedPane.setForeground(UIManager.getTextPrimary());
    }
    
    /**
     * Configure la barre de menu
     */
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(UIManager.getSurface());
        
        // Menu Fichier
        fileMenu = new JMenu("Fichier");
        logoutItem = new JMenuItem("Déconnexion");
        JMenuItem exitItem = new JMenuItem("Quitter");
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Menu Édition
        editMenu = new JMenu("Édition");
        JMenuItem preferencesItem = new JMenuItem("Préférences");
        editMenu.add(preferencesItem);
        
        // Menu Affichage
        viewMenu = new JMenu("Affichage");
        JMenuItem darkThemeItem = new JMenuItem("Thème sombre");
        darkThemeItem.addActionListener(e -> {
            UIManager.setTheme(UIManager.Theme.DARK);
            UIManager.applyThemeRecursively(this);
            UIManager.saveTheme(UIManager.Theme.DARK);
            com.securephone.client.utils.SoundPlayer.getInstance().playButtonClick();
            com.securephone.client.utils.NotificationManager.getInstance().showSuccessNotification(
                "Thème changé",
                "Thème sombre appliqué"
            );
        });
        JMenuItem lightThemeItem = new JMenuItem("Thème clair");
        lightThemeItem.addActionListener(e -> {
            UIManager.setTheme(UIManager.Theme.LIGHT);
            UIManager.applyThemeRecursively(this);
            UIManager.saveTheme(UIManager.Theme.LIGHT);
            com.securephone.client.utils.SoundPlayer.getInstance().playButtonClick();
            com.securephone.client.utils.NotificationManager.getInstance().showSuccessNotification(
                "Thème changé",
                "Thème clair appliqué"
            );
        });
        viewMenu.add(darkThemeItem);
        viewMenu.add(lightThemeItem);
        
        // Menu Aide
        helpMenu = new JMenu("Aide");
        JMenuItem aboutItem = new JMenuItem("À propos");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "SecurePhone v1.0.0\nApplication de messagerie sécurisée",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);
        
        // Ajouter les menus
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    // ========== GETTERS POUR LES FRAMES ==========
    public LoginFrame getLoginFrame() {
        return loginFrame;
    }
    
    public ChatFrame getChatFrame() {
        return chatFrame;
    }
    
    public ContactFrame getContactFrame() {
        return contactFrame;
    }
    
    public SettingsFrame getSettingsFrame() {
        return settingsFrame;
    }

    public void onLogoutClick(Runnable action) {
        logoutItem.addActionListener(e -> action.run());
    }
    
    // ========== NAVIGATION ==========
    public void showLoginTab() {
        tabbedPane.setSelectedIndex(0);
    }
    
    public void showChatTab() {
        tabbedPane.setSelectedIndex(1);
    }
    
    public void showContactsTab() {
        tabbedPane.setSelectedIndex(2);
    }
    
    public void showSettingsTab() {
        tabbedPane.setSelectedIndex(3);
    }
    
    /**
     * Lance l'application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
