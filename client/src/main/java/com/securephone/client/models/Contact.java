package com.securephone.client.models;

/**
 * Contact - Modèle d'un contact
 * 
 * Représente un contact dans la liste de contacts
 * 
 * @author Hatsu
 */
public class Contact {
    
    private String id;
    private String name;
    private String phoneNumber;
    private String status;
    private String addedDate;
    
    // ========== CONSTRUCTEUR ==========
    public Contact(String id, String name, String phoneNumber, String status, String addedDate) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.addedDate = addedDate;
    }
    
    public Contact(String name, String phoneNumber, String status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.addedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
    }
    
    // ========== GETTERS ==========
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getAddedDate() {
        return addedDate;
    }
    
    // ========== SETTERS ==========
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contact contact = (Contact) obj;
        return name.equals(contact.name);
    }
}
