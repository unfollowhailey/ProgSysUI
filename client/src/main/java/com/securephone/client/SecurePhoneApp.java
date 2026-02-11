package com.securephone.client;

import javax.swing.*;
import com.securephone.client.ui.frames.MainFrame;
import com.securephone.client.utils.SoundPlayer;
import com.securephone.client.utils.NotificationManager;
import com.securephone.client.network.ConnectionManager;
import com.securephone.client.models.ChatMessage;
import com.securephone.client.models.Contact;
import java.util.List;

public class SecurePhoneApp {
    
    // ========== VARIABLES STATIQUES ==========
    private static MainFrame mainFrame;
    private static ConnectionManager connectionManager;
    private static boolean audioEnabled = false;
    private static boolean videoEnabled = false;
    private static JFrame videoFrame;
    private static com.securephone.client.models.Contact activeContact;
    
    // ========== POINT D'ENTRÉE ==========
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de SecurePhone...");
        System.out.println("═══════════════════════════════════════");
        
        // Configuration Swing (doit être dans Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialiser le Look and Feel système
                try {
                    javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName()
                    );
                } catch (Exception e) {
                    System.err.println("   ⚠ Impossible de définir le Look and Feel système");
                }
                
                // Initialiser les ressources
                initializeResources();
                
                // Créer et afficher la fenêtre principale
                mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                
                // Initialiser les listeners
                initializeListeners();
                initializeNetwork();
                
                System.out.println("✅ Application démarrée avec succès!");
                System.out.println("═══════════════════════════════════════");
                printAppInfo();
                
                // Sauvegarder le thème à la fermeture
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    com.securephone.client.ui.UIManager.saveTheme(
                        com.securephone.client.ui.UIManager.getCurrentTheme()
                    );
                    System.out.println("💾 Thème sauvegardé");
                }));
                
            } catch (Exception e) {
                System.err.println("❌ Erreur au démarrage de l'application:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    "Erreur au démarrage:\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
    
    /**
     * Initialise les ressources (sons, notifications, etc.)
     */
    private static void initializeResources() {
        System.out.println("\n🔧 Initialisation des ressources...");
        
        // Initialiser SoundPlayer
        try {
            SoundPlayer soundPlayer = SoundPlayer.getInstance();
            System.out.println("   ✓ SoundPlayer initialisé");
            System.out.println("   ✓ Volume: " + (int)(soundPlayer.getMasterVolume() * 100) + "%");
        } catch (Exception e) {
            System.err.println("   ✗ Erreur SoundPlayer: " + e.getMessage());
        }
        
        // Initialiser NotificationManager
        try {
            NotificationManager.getInstance();
            System.out.println("   ✓ NotificationManager initialisé");
        } catch (Exception e) {
            System.err.println("   ✗ Erreur NotificationManager: " + e.getMessage());
        }
        
        // Initialiser UIManager avec le thème
        try {
            // Le thème sauvegardé est chargé automatiquement dans UIManager
            // On récupère juste le thème actuel
            com.securephone.client.ui.UIManager.Theme currentTheme = 
                com.securephone.client.ui.UIManager.getCurrentTheme();
            System.out.println("   ✓ UIManager initialisé (thème: " + currentTheme + ")");
        } catch (Exception e) {
            System.err.println("   ✗ Erreur UIManager: " + e.getMessage());
        }
    }
    
    /**
     * Initialise les listeners et callbacks pour démonstration
     */
    private static void initializeListeners() {
        System.out.println("\n🎯 Configuration des listeners...");
        
        // Récupérer les frames
        var loginFrame = mainFrame.getLoginFrame();
        var chatFrame = mainFrame.getChatFrame();
        var contactFrame = mainFrame.getContactFrame();
        var settingsFrame = mainFrame.getSettingsFrame();
        
        // ========== LOGIN FRAME ==========
        loginFrame.onLoginButtonClick(() -> {
            String username = loginFrame.getUsername();
            String password = loginFrame.getPassword();
            String totp = loginFrame.getTOTPCode();
            
            // Son de clic
            SoundPlayer.getInstance().playButtonClick();
            
            if (username.isEmpty() || password.isEmpty()) {
                loginFrame.setError("❌ Veuillez remplir tous les champs");
                SoundPlayer.getInstance().playError();
                NotificationManager.getInstance().showErrorNotification(
                    "Erreur de connexion",
                    "Veuillez remplir tous les champs"
                );
                return;
            }
            
            loginFrame.clearError();
            if (connectionManager != null) {
                if (!connectionManager.connect()) {
                    loginFrame.setError("Serveur indisponible");
                    NotificationManager.getInstance().showErrorNotification(
                        "Connexion",
                        "Serveur indisponible"
                    );
                    return;
                }
                connectionManager.login(username, password, totp);
            }
        });
        
        loginFrame.onRegisterButtonClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            String username = loginFrame.getUsername();
            String password = loginFrame.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                loginFrame.setError("Veuillez remplir username et mot de passe");
                return;
            }

            if (connectionManager != null) {
                if (!connectionManager.connect()) {
                    loginFrame.setError("Serveur indisponible");
                    return;
                }
                connectionManager.register(username, password);
            }
        });
        
        // ========== CHAT FRAME ==========
        chatFrame.onSendButtonClick(() -> {
            String message = chatFrame.getInputText();
            
            if (message.isEmpty()) return;

            if (activeContact == null) {
                NotificationManager.getInstance().showWarningNotification(
                    "Contact requis",
                    "Selectionnez un contact avant d'envoyer un message"
                );
                return;
            }
            
            chatFrame.addMessage(new ChatMessage("Moi", message, System.currentTimeMillis(), ""));
            chatFrame.clearInput();
            SoundPlayer.getInstance().playMessageSent();
            if (connectionManager != null) {
                connectionManager.sendChatMessage(message, activeContact.getName());
            }
        });
        
        chatFrame.onPTTStart(() -> {
            SoundPlayer.getInstance().playButtonClick();
            if (connectionManager != null) {
                connectionManager.startAudio();
            }
        });

        chatFrame.onPTTStop(duration -> {
            SoundPlayer.getInstance().playButtonClick();
            if (connectionManager != null) {
                connectionManager.stopAudio();
            }
        });
        
        chatFrame.onAudioToggleClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            if (connectionManager == null) {
                return;
            }
            if (!audioEnabled) {
                connectionManager.startAudio();
                audioEnabled = true;
                NotificationManager.getInstance().showInfoNotification(
                    "Audio",
                    "Audio active"
                );
            } else {
                connectionManager.stopAudio();
                audioEnabled = false;
                NotificationManager.getInstance().showInfoNotification(
                    "Audio",
                    "Audio desactive"
                );
            }
        });
        
        chatFrame.onVideoToggleClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            if (connectionManager == null) {
                return;
            }
            if (!videoEnabled) {
                startVideoWindow();
                connectionManager.startVideo();
                videoEnabled = true;
            } else {
                connectionManager.stopVideo();
                stopVideoWindow();
                videoEnabled = false;
            }
        });
        
        // ========== CONTACT FRAME ==========
        // Ajouter des contacts de démonstration si pas de serveur
        addDemoContacts(contactFrame);

        contactFrame.onContactSelected(() -> {
            activeContact = contactFrame.getSelectedContact();
            String name = activeContact != null ? activeContact.getName() : null;
            chatFrame.setActiveContact(name);
            if (name != null) {
                mainFrame.showChatTab();
            }
        });
        
        contactFrame.onAddContactClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            String phone = JOptionPane.showInputDialog("Numéro téléphone:");
            if (phone != null && !phone.isEmpty()) {
                System.out.println("[CONTACTS] Ajout contact: " + phone);
                SoundPlayer.getInstance().playMessageSent();
                NotificationManager.getInstance().showSuccessNotification(
                    "Contact ajouté",
                    "Demande envoyee"
                );
            }
        });
        
        contactFrame.onDeleteContactClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            var selected = contactFrame.getSelectedContact();
            if (selected != null) {
                System.out.println("[CONTACTS] Suppression: " + selected.getName());
                SoundPlayer.getInstance().playMessageSent();
                NotificationManager.getInstance().showSuccessNotification(
                    "Contact supprimé",
                    "Contact supprime"
                );
            }
        });
        
        // ========== SETTINGS FRAME ==========
        settingsFrame.onSaveClick(() -> {
            System.out.println("[SETTINGS] Paramètres sauvegardés:");
            
            // Appliquer le volume
            float volume = settingsFrame.getMasterVolume() / 100.0f;
            SoundPlayer.getInstance().setMasterVolume(volume);
            System.out.println("   • Volume: " + settingsFrame.getMasterVolume() + "%");
            
            // Appliquer les sons
            boolean soundsEnabled = settingsFrame.isSoundsEnabled();
            SoundPlayer.getInstance().setSoundEnabled(soundsEnabled);
            System.out.println("   • Sounds: " + (soundsEnabled ? "ON" : "OFF"));
            
            // Appliquer les notifications
            boolean notifEnabled = settingsFrame.isNotificationsEnabled();
            NotificationManager.getInstance().setNotificationsEnabled(notifEnabled);
            System.out.println("   • Notifications: " + (notifEnabled ? "ON" : "OFF"));
            
            System.out.println("   • Resolution: " + settingsFrame.getResolution());
            System.out.println("   • Theme: " + settingsFrame.getTheme());
            
            // Appliquer le thème sélectionné avec application récursive
            String theme = settingsFrame.getTheme();
            com.securephone.client.ui.UIManager.Theme newTheme;
            if (theme.equals("Clair")) {
                newTheme = com.securephone.client.ui.UIManager.Theme.LIGHT;
            } else {
                newTheme = com.securephone.client.ui.UIManager.Theme.DARK;
            }
            
            // Changement de thème avec application récursive (comme dans MainUIDemo)
            com.securephone.client.ui.UIManager.setTheme(newTheme);
            com.securephone.client.ui.UIManager.applyThemeRecursively(mainFrame);
            System.out.println("   → Thème changé: " + newTheme + " (appliqué récursivement)");
            
            // Sauvegarder le thème
            com.securephone.client.ui.UIManager.saveTheme(newTheme);
            
            // Feedback sonore et visuel
            SoundPlayer.getInstance().playMessageSent();
            NotificationManager.getInstance().showSuccessNotification(
                "Paramètres sauvegardés",
                "Vos préférences ont été appliquées avec succès"
            );
        });
        
        settingsFrame.onResetClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            System.out.println("[SETTINGS] Réinitialisation...");
            NotificationManager.getInstance().showInfoNotification(
                "Réinitialisation",
                "Paramètres réinitialisés (simulation)"
            );
        });
        
        settingsFrame.onChangePasswordClick(() -> {
            SoundPlayer.getInstance().playButtonClick();
            String newPassword = JOptionPane.showInputDialog("Nouveau mot de passe:");
            if (newPassword != null && !newPassword.isEmpty()) {
                System.out.println("[SETTINGS] Mot de passe changé");
                SoundPlayer.getInstance().playMessageSent();
                NotificationManager.getInstance().showSuccessNotification(
                    "Mot de passe changé",
                    "Votre mot de passe a été mis à jour avec succès"
                );
            }
        });
        
        System.out.println("   ✓ Tous les listeners configurés");
    }

    private static void initializeNetwork() {
        connectionManager = new ConnectionManager();

        connectionManager.setAuthListener(new ConnectionManager.AuthListener() {
            @Override
            public void onLoginSuccess(com.securephone.client.models.UserSession session) {
                SwingUtilities.invokeLater(() -> {
                    NotificationManager.getInstance().showSuccessNotification(
                        "Connexion reussie",
                        "Bienvenue " + session.getUsername() + "!"
                    );
                    mainFrame.getLoginFrame().clearFields();
                    mainFrame.showChatTab();
                    connectionManager.requestContacts();
                });
            }

            @Override
            public void onLoginRequires2FA(String message) {
                SwingUtilities.invokeLater(() -> {
                    mainFrame.getLoginFrame().setError(message);
                    String code = extract2faCode(message);
                    if (code != null) {
                        mainFrame.getLoginFrame().setTOTPCode(code);
                        JOptionPane.showMessageDialog(
                            mainFrame,
                            "Code 2FA (dev): " + code,
                            "2FA",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    NotificationManager.getInstance().showInfoNotification("2FA", message);
                });
            }

            @Override
            public void onLoginFailed(String reason) {
                SwingUtilities.invokeLater(() -> {
                    mainFrame.getLoginFrame().setError(reason);
                    NotificationManager.getInstance().showErrorNotification(
                        "Connexion",
                        reason
                    );
                });
            }

            @Override
            public void onRegisterSuccess(String message) {
                SwingUtilities.invokeLater(() -> NotificationManager.getInstance().showSuccessNotification(
                    "Inscription",
                    message
                ));
            }

            @Override
            public void onRegisterFailed(String reason) {
                SwingUtilities.invokeLater(() -> NotificationManager.getInstance().showErrorNotification(
                    "Inscription",
                    reason
                ));
            }
        });

        connectionManager.setChatListener(message -> SwingUtilities.invokeLater(() -> {
            mainFrame.getChatFrame().addMessage(message);
            SoundPlayer.getInstance().playMessageReceived();
            NotificationManager.getInstance().showMessageNotification(
                message.getSender(),
                message.getContent()
            );
        }));

        mainFrame.onLogoutClick(() -> {
            if (connectionManager != null) {
                connectionManager.logout();
            }
            mainFrame.getChatFrame().clearMessages();
            mainFrame.getLoginFrame().clearFields();
            mainFrame.getContactFrame().clearSelection();
            activeContact = null;
            mainFrame.getChatFrame().setActiveContact(null);
            mainFrame.showLoginTab();
            NotificationManager.getInstance().showInfoNotification(
                "Deconnexion",
                "Vous etes deconnecte"
            );
        });

        connectionManager.setContactListener(contacts -> SwingUtilities.invokeLater(() -> {
            var contactFrame = mainFrame.getContactFrame();
            contactFrame.clearContacts();
            for (Contact contact : contacts) {
                contactFrame.addContactToList(contact);
            }
        }));

        connectionManager.setStatusListener(status -> SwingUtilities.invokeLater(() -> {
            mainFrame.getChatFrame().setStatus(status);
        }));

        connectionManager.setErrorListener(message -> SwingUtilities.invokeLater(() -> {
            NotificationManager.getInstance().showErrorNotification("Erreur", message);
        }));
    }
    
    /**
     * Ajoute des contacts de démonstration
     */
    private static void addDemoContacts(com.securephone.client.ui.frames.ContactFrame contactFrame) {
        var contacts = new String[][] {
            {"Alice Dupont", "online"},
            {"Bob Martin", "away"},
            {"Charlie Brown", "offline"},
            {"Diana Prince", "online"},
            {"Eve Johnson", "online"}
        };
        
        for (var contact : contacts) {
            var c = new com.securephone.client.models.Contact(
                contact[0],
                "+33 6 12 34 56 78",
                contact[1]
            );
            contactFrame.addContactToList(c);
        }
        
        System.out.println("   ✓ " + contacts.length + " contacts de démonstration ajoutés");
    }
    
    /**
     * Affiche les informations de l'application
     */
    private static void printAppInfo() {
        System.out.println("\n📱 INFOS APPLICATI ON");
        System.out.println("   App: SecurePhone v1.0.0");
        System.out.println("   Status: UI Ready");
        System.out.println("   Thème: Dark (configurable)");
        System.out.println("   Audio: Activé");
        System.out.println("   Notifications: Activées");
        System.out.println("\n💡 CONSEILS DE TEST");
        System.out.println("   1. Login: username='test', password='test'");
        System.out.println("   2. Chat: Écrivez un message et envoyez");
        System.out.println("   3. Contacts: Cliquez sur les contacts pour voir les détails");
        System.out.println("   4. Settings: Changez le thème et le volume");
        System.out.println("   5. Menu: Utilisez le menu pour naviguer");
        System.out.println("\n⚠️  LIMITATIONS ACTUELLES");
        System.out.println("   • Camera reelle non integree (flux video simule)");
        System.out.println("   • Donnees non persistantes (memoire serveur)");
        System.out.println("\n🎯 POUR LANCER");
        System.out.println("   mvn exec:java -Dexec.mainClass=\"com.securephone.client.SecurePhoneApp\"");
        System.out.println("═══════════════════════════════════════\n");
    }

    private static void startVideoWindow() {
        if (videoFrame != null) {
            return;
        }

        videoFrame = new JFrame("SecurePhone Video");
        com.securephone.client.video.VideoPlayer player = new com.securephone.client.video.VideoPlayer();
        videoFrame.setSize(640, 480);
        videoFrame.setLocationRelativeTo(null);
        videoFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        videoFrame.add(player);
        videoFrame.setVisible(true);

        if (connectionManager != null) {
            connectionManager.getVideoClient().setFrameListener(player::setFrame);
        }
    }

    private static String extract2faCode(String message) {
        if (message == null) {
            return null;
        }
        int idx = message.indexOf("code:");
        if (idx < 0) {
            return null;
        }
        String tail = message.substring(idx + "code:".length()).trim();
        int end = tail.indexOf(")");
        String code = end > 0 ? tail.substring(0, end).trim() : tail;
        return code.isEmpty() ? null : code;
    }

    private static void stopVideoWindow() {
        if (videoFrame != null) {
            videoFrame.setVisible(false);
            videoFrame.dispose();
            videoFrame = null;
        }
    }
}
