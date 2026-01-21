-- ============================================
-- tables_complete.sql
-- Base de données COMPLÈTE pour SecurePhone
-- Avec PUSH notifications et support vidéo
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS securephone;
USE securephone;

-- ========== TABLE UTILISATEURS ==========
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    twofa_enabled BOOLEAN DEFAULT TRUE,
    status ENUM('online', 'offline', 'away', 'busy') DEFAULT 'offline',
    last_seen TIMESTAMP NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== TABLE MESSAGES ==========
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    sender_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    room_id VARCHAR(100) DEFAULT NULL, -- Pour les groupes
    message_type ENUM('text', 'image', 'file', 'audio', 'video') DEFAULT 'text',
    content TEXT NOT NULL,
    encrypted BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_status BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_room (room_id)
);

-- ========== TABLE CONTACTS ==========
CREATE TABLE contacts (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER NOT NULL,
    contact_id INTEGER NOT NULL,
    nickname VARCHAR(50),
    favorite BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_contact (user_id, contact_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========== TABLE PUSH NOTIFICATIONS ==========
CREATE TABLE device_tokens (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER NOT NULL,
    device_token VARCHAR(255) NOT NULL,
    platform ENUM('android', 'ios', 'web', 'desktop') NOT NULL,
    app_version VARCHAR(20) DEFAULT '1.0.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    UNIQUE KEY unique_device_token (device_token),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_tokens (user_id, active)
);

-- ========== TABLE ROOMS (pour audio/vidéo groupe) ==========
CREATE TABLE rooms (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    creator_id INTEGER NOT NULL,
    room_type ENUM('audio', 'video', 'conference') DEFAULT 'audio',
    max_participants INTEGER DEFAULT 10,
    is_private BOOLEAN DEFAULT FALSE,
    password_hash VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_room_type (room_type, is_private)
);

-- ========== TABLE ROOM MEMBERS ==========
CREATE TABLE room_members (
    room_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE,
    is_muted BOOLEAN DEFAULT FALSE,
    
    PRIMARY KEY (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========== TABLE APPELS ==========
CREATE TABLE calls (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    caller_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    room_id INTEGER DEFAULT NULL,
    call_type ENUM('audio', 'video') NOT NULL,
    status ENUM('initiated', 'ringing', 'answered', 'rejected', 'missed', 'ended') DEFAULT 'initiated',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration INTEGER DEFAULT 0, -- en secondes
    encryption_key VARCHAR(255) DEFAULT NULL,
    
    FOREIGN KEY (caller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    INDEX idx_call_status (status, start_time)
);

-- ========== TABLE NOTIFICATIONS ==========
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER NOT NULL,
    type ENUM('message', 'call', 'contact_request', 'system') NOT NULL,
    title VARCHAR(100) NOT NULL,
    body TEXT NOT NULL,
    data JSON DEFAULT NULL,
    read_status BOOLEAN DEFAULT FALSE,
    sent_via ENUM('push', 'in_app', 'both') DEFAULT 'in_app',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_notifications (user_id, read_status, created_at)
);

-- ========== INDEX POUR PERFORMANCE ==========
CREATE INDEX idx_users_status ON users(status, last_seen);
CREATE INDEX idx_messages_timestamp ON messages(timestamp DESC);
CREATE INDEX idx_messages_conversation ON messages(
    LEAST(sender_id, receiver_id), 
    GREATEST(sender_id, receiver_id), 
    timestamp DESC
);
CREATE INDEX idx_calls_participants ON calls(caller_id, receiver_id, start_time DESC);
CREATE INDEX idx_device_last_used ON device_tokens(last_used DESC);
CREATE INDEX idx_room_activity ON rooms(last_activity DESC);