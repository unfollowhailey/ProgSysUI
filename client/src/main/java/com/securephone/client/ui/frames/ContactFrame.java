package com.securephone.client.ui.frames;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.securephone.client.ui.UIManager;
import com.securephone.client.models.Contact;

/**
 * ContactFrame - Gestion des contacts
 * 
 * Responsabilités:
 * - Afficher liste des contacts
 * - Ajouter/modifier/supprimer contacts
 * - Chercher et filtrer contacts
 * - Afficher détails d'un contact
 * 
 * Dépendances:
 * - Contact.java (modèle partagé)
 * - ContactServlet.java (Hailey) pour API /contacts
 * - ContactList.java (composant Hatsu)
 * 
 * @author Hatsu
 */
public class ContactFrame extends JPanel {
    
    // ========== COMPOSANTS UI ==========
    private JTextField searchField;
    private JButton addContactButton;
    private JButton deleteContactButton;
    private JButton editContactButton;
    
    private JList<String> contactListDisplay;
    private JTextArea contactDetailsArea;
    
    private JLabel statusLabel;
    
    // ========== VARIABLES ==========
    private List<Contact> contacts = new ArrayList<>();
    private Contact selectedContact;
    
    // ========== CONSTRUCTEUR ==========
    public ContactFrame() {
        initComponents();
        setupLayout();
        setupTheme();
    }
    
    /**
     * Initialise les composants UI
     */
    private void initComponents() {
        // Recherche
        searchField = new JTextField(20);
        UIManager.styleTextField(searchField);
        
        // Boutons d'action
        addContactButton = new JButton("➕ Ajouter");
        deleteContactButton = new JButton("🗑️ Supprimer");
        editContactButton = new JButton("✏️ Modifier");
        
        // Liste des contacts
        contactListDisplay = new JList<>();
        contactListDisplay.setBackground(UIManager.getSurface());
        contactListDisplay.setForeground(UIManager.getTextPrimary());
        
        // Détails du contact
        contactDetailsArea = new JTextArea();
        contactDetailsArea.setEditable(false);
        contactDetailsArea.setLineWrap(true);
        contactDetailsArea.setWrapStyleWord(true);
        
        // Status
        statusLabel = new JLabel("Contacts chargés");
        
        // Style
        UIManager.stylePrimaryButton(addContactButton);
        UIManager.styleSecondaryButton(deleteContactButton);
        UIManager.styleSecondaryButton(editContactButton);
    }
    
    /**
     * Configure le layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Haut: Recherche + Boutons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getSurface());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UIManager.getSurface());
        JLabel searchLabel = new JLabel("Chercher:");
        UIManager.styleBody(searchLabel);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(UIManager.getSurface());
        buttonsPanel.add(addContactButton);
        buttonsPanel.add(editContactButton);
        buttonsPanel.add(deleteContactButton);
        topPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Centre: Split entre liste et détails
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(contactListDisplay));
        splitPane.setRightComponent(new JScrollPane(contactDetailsArea));
        splitPane.setDividerLocation(0.3);
        add(splitPane, BorderLayout.CENTER);
        
        // Bas: Status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIManager.getSurface());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Applique le thème
     */
    private void setupTheme() {
        UIManager.stylePanel(this);
        contactDetailsArea.setBackground(UIManager.getSurface());
        contactDetailsArea.setForeground(UIManager.getTextPrimary());
    }
    
    // ========== MÉTHODES PUBLIQUES ==========
    
    /**
     * Ajoute un contact à la liste
     */
    public void addContactToList(Contact contact) {
        contacts.add(contact);
        refreshContactList();
    }
    
    /**
     * Met à jour l'affichage de la liste
     */
    private void refreshContactList() {
        String[] names = contacts.stream()
            .map(Contact::getName)
            .toArray(String[]::new);
        contactListDisplay.setListData(names);
    }
    
    /**
     * Affiche les détails du contact sélectionné
     */
    public void showContactDetails(Contact contact) {
        selectedContact = contact;
        StringBuilder details = new StringBuilder();
        details.append("Nom: ").append(contact.getName()).append("\n");
        details.append("Téléphone: ").append(contact.getPhoneNumber()).append("\n");
        details.append("Statut: ").append(contact.getStatus()).append("\n");
        details.append("Depuis: ").append(contact.getAddedDate()).append("\n");
        contactDetailsArea.setText(details.toString());
    }
    
    /**
     * Filtre les contacts par recherche
     */
    public void searchContacts(String query) {
        String[] names = contacts.stream()
            .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()))
            .map(Contact::getName)
            .toArray(String[]::new);
        contactListDisplay.setListData(names);
    }
    
    /**
     * Récupère le contact sélectionné
     */
    public Contact getSelectedContact() {
        return selectedContact;
    }
    
    /**
     * Vide la liste
     */
    public void clearContacts() {
        contacts.clear();
        contactListDisplay.setListData(new String[0]);
        contactDetailsArea.setText("");
    }

    public void clearSelection() {
        contactListDisplay.clearSelection();
        selectedContact = null;
        contactDetailsArea.setText("");
    }
    
    // ========== LISTENERS ==========
    public void onAddContactClick(Runnable action) {
        addContactButton.addActionListener(e -> action.run());
    }
    
    public void onDeleteContactClick(Runnable action) {
        deleteContactButton.addActionListener(e -> action.run());
    }
    
    public void onEditContactClick(Runnable action) {
        editContactButton.addActionListener(e -> action.run());
    }
    
    public void onContactSelected(Runnable action) {
        contactListDisplay.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = contactListDisplay.getSelectedValue();
                selectedContact = findContactByName(selectedName);
                if (selectedContact != null) {
                    showContactDetails(selectedContact);
                }
                action.run();
            }
        });
    }
    
    public void onSearchInput(Runnable action) {
        searchField.addActionListener(e -> action.run());
    }

    private Contact findContactByName(String name) {
        if (name == null) {
            return null;
        }
        for (Contact contact : contacts) {
            if (name.equals(contact.getName())) {
                return contact;
            }
        }
        return null;
    }
}
