package com.securephone.client.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.securephone.client.ui.UIManager;
import com.securephone.client.models.Contact;

/**
 * ContactList - Composant pour afficher la liste des contacts
 * 
 * Responsabilités:
 * - Afficher les contacts avec avatar et statut
 * - Gestion de la sélection
 * - Support de la recherche/filtrage
 * - Indicateur de statut (en ligne, absent, hors ligne)
 * 
 * Dépendances:
 * - Contact.java (modèle)
 * 
 * @author Hatsu
 */
public class ContactList extends JPanel {
    
    // ========== COMPOSANTS ==========
    private JList<ContactListItem> contactJList;
    private DefaultListModel<ContactListItem> listModel;
    private JTextField searchField;
    
    // ========== VARIABLES ==========
    private List<Contact> contacts = new ArrayList<>();
    private List<ContactListItem> displayedItems = new ArrayList<>();
    
    // ========== CONSTRUCTEUR ==========
    public ContactList() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants
     */
    private void initComponents() {
        // Model de liste
        listModel = new DefaultListModel<>();
        
        // Liste
        contactJList = new JList<>(listModel);
        contactJList.setCellRenderer(new ContactListCellRenderer());
        contactJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Champ de recherche
        searchField = new JTextField("Chercher un contact...");
        UIManager.styleTextField(searchField);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Haut: Champ de recherche
        add(searchField, BorderLayout.NORTH);
        
        // Centre: Liste des contacts
        JScrollPane scrollPane = new JScrollPane(contactJList);
        scrollPane.setBackground(UIManager.getSurface());
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
        contactJList.setBackground(UIManager.getSurface());
        contactJList.setForeground(UIManager.getTextPrimary());
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    /**
     * Ajoute un contact à la liste
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
        ContactListItem item = new ContactListItem(contact);
        displayedItems.add(item);
        listModel.addElement(item);
    }
    
    /**
     * Supprime un contact de la liste
     */
    public void removeContact(Contact contact) {
        contacts.remove(contact);
        displayedItems.removeIf(item -> item.contact.equals(contact));
        updateListDisplay();
    }
    
    /**
     * Récupère le contact sélectionné
     */
    public Contact getSelectedContact() {
        ContactListItem selected = contactJList.getSelectedValue();
        return selected != null ? selected.contact : null;
    }
    
    /**
     * Filtre la liste par recherche
     */
    public void filterBySearch(String query) {
        listModel.clear();
        String lowerQuery = query.toLowerCase();
        
        for (ContactListItem item : displayedItems) {
            if (item.contact.getName().toLowerCase().contains(lowerQuery)) {
                listModel.addElement(item);
            }
        }
    }
    
    /**
     * Réinitialise le filtre
     */
    public void resetFilter() {
        listModel.clear();
        for (ContactListItem item : displayedItems) {
            listModel.addElement(item);
        }
    }
    
    /**
     * Met à jour l'affichage
     */
    private void updateListDisplay() {
        listModel.clear();
        for (ContactListItem item : displayedItems) {
            listModel.addElement(item);
        }
    }
    
    /**
     * Vide la liste
     */
    public void clear() {
        contacts.clear();
        displayedItems.clear();
        listModel.clear();
    }
    
    // ========== LISTENERS ==========
    public void onContactSelected(Runnable action) {
        contactJList.addListSelectionListener(e -> action.run());
    }
    
    public void onSearchInput(javax.swing.event.DocumentListener listener) {
        searchField.getDocument().addDocumentListener(listener);
    }
    
    // ========== CLASSE INTERNE ==========
    
    /**
     * Représente un élément dans la liste
     */
    static class ContactListItem {
        Contact contact;
        
        ContactListItem(Contact contact) {
            this.contact = contact;
        }
        
        @Override
        public String toString() {
            return contact.getName();
        }
    }
    
    /**
     * Renderer personnalisé pour afficher les contacts avec avatar et statut
     */
    static class ContactListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            
            if (value instanceof ContactListItem) {
                ContactListItem item = (ContactListItem) value;
                Contact contact = item.contact;
                
                // Avatar
                JLabel avatarLabel = new JLabel(getInitials(contact.getName()));
                avatarLabel.setFont(UIManager.Fonts.BODY_BOLD);
                avatarLabel.setHorizontalAlignment(JLabel.CENTER);
                avatarLabel.setOpaque(true);
                avatarLabel.setBackground(UIManager.getPrimary());
                avatarLabel.setForeground(Color.WHITE);
                avatarLabel.setPreferredSize(new Dimension(32, 32));
                
                // Info: nom et statut
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                
                JLabel nameLabel = new JLabel(contact.getName());
                nameLabel.setFont(UIManager.Fonts.BODY_BOLD);
                infoPanel.add(nameLabel);
                
                JLabel statusLabel = new JLabel(getStatusIcon(contact.getStatus()) + " " + contact.getStatus());
                statusLabel.setFont(UIManager.Fonts.CAPTION);
                infoPanel.add(statusLabel);
                
                panel.add(avatarLabel, BorderLayout.WEST);
                panel.add(infoPanel, BorderLayout.CENTER);
            }
            
            if (isSelected) {
                panel.setBackground(UIManager.getSurface());
                panel.setOpaque(true);
            }
            
            return panel;
        }
        
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
        
        private String getStatusIcon(String status) {
            String lower = status.toLowerCase();
            if (lower.equals("online") || lower.equals("en ligne")) {
                return "🟢";
            } else if (lower.equals("away") || lower.equals("absent")) {
                return "🟡";
            } else if (lower.equals("offline") || lower.equals("hors ligne")) {
                return "⚫";
            }
            return "❓";
        }
    }
}
