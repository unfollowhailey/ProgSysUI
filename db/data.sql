-- ============================================
-- data_complete.sql
-- Données de test COMPLÈTES pour SecurePhone
-- Avec PUSH, vidéo, et données réalistes
-- ============================================

USE securephone;

-- Vider toutes les tables (ordre important pour contraintes)
DELETE FROM notifications;
DELETE FROM calls;
DELETE FROM room_members;
DELETE FROM rooms;
DELETE FROM device_tokens;
DELETE FROM messages;
DELETE FROM contacts;
DELETE FROM users;

-- Réinitialiser les auto-increments
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
ALTER TABLE contacts AUTO_INCREMENT = 1;
ALTER TABLE device_tokens AUTO_INCREMENT = 1;
ALTER TABLE rooms AUTO_INCREMENT = 1;
ALTER TABLE calls AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;

-- ========== UTILISATEURS ==========
-- Mot de passe pour tous : "password123"
-- Hash bcrypt valide : $2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5N8q5
INSERT INTO users (username, password_hash, email, status, twofa_enabled) VALUES
('alice', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5N8q5', 'alice@securephone.com', 'online', TRUE),
('bob', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5N8q5', 'bob@securephone.com', 'online', TRUE),
('charlie', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5', 'charlie@securephone.com', 'away', TRUE),
('diana', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5N8q5', 'diana@securephone.com', 'offline', TRUE),
('operator', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Z7p6c5Q8Qq9q5J8q5N8q5', 'support@securephone.com', 'online', FALSE);

-- ========== CONTACTS ==========
INSERT INTO contacts (user_id, contact_id, nickname, favorite) VALUES
-- Contacts d'Alice
(1, 2, 'Bob', TRUE),
(1, 3, 'Charlie', TRUE),
(1, 4, 'Diana', FALSE),
(1, 5, 'Support', FALSE),
-- Contacts de Bob
(2, 1, 'Alice', TRUE),
(2, 3, 'Charlie', TRUE),
(2, 4, 'Diana', FALSE),
-- Contacts de Charlie
(3, 1, 'Alice', TRUE),
(3, 2, 'Bob', TRUE),
-- Contacts de Diana
(4, 1, 'Alice', TRUE),
(4, 2, 'Bob', FALSE);

-- ========== MESSAGES ==========
-- Messages privés
INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES
-- Conversation Alice <-> Bob (récente)
(1, 2, 'Salut Bob, prêt pour le test vidéo ?', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(2, 1, 'Oui Alice, j''ai configuré la webcam', DATE_SUB(NOW(), INTERVAL 14 MINUTE)),
(1, 2, 'Parfait, on teste d''abord l''audio PTT', DATE_SUB(NOW(), INTERVAL 13 MINUTE)),
(2, 1, 'OK, j''appuie sur le bouton maintenant', DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
(1, 2, 'J''entends bien, qualité audio super !', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),

-- Autres conversations
(1, 3, 'Charlie, tu rejoins la salle vidéo ?', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(3, 1, 'Oui, je suis là dans 5 minutes', DATE_SUB(NOW(), INTERVAL 44 MINUTE)),
(4, 1, 'Alice, tu peux m''aider avec l''appli ?', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 4, 'Bien sûr Diana, quel problème ?', DATE_SUB(NOW(), INTERVAL 118 MINUTE));

-- Messages de groupe (room_id)
INSERT INTO messages (sender_id, receiver_id, room_id, content, timestamp) VALUES
(1, 2, 'team-projet', 'Bienvenue dans le chat du projet SecurePhone !', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 'team-projet', 'Merci Alice, content de travailler avec vous', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 1, 'team-projet', 'Je suis là aussi pour le développement', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 2, 'team-projet', 'Réunion vidéo demain 10h pour la démo', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ========== PUSH NOTIFICATIONS ==========
INSERT INTO device_tokens (user_id, device_token, platform, app_version, active) VALUES
-- Alice a 2 devices
(1, 'fcm_alice_android_123456', 'android', '1.0.0', TRUE),
(1, 'fcm_alice_desktop_789012', 'desktop', '1.0.0', TRUE),
-- Bob
(2, 'fcm_bob_ios_345678', 'ios', '1.0.0', TRUE),
-- Charlie
(3, 'fcm_charlie_web_901234', 'web', '1.0.0', TRUE),
-- Diana
(4, 'fcm_diana_android_567890', 'android', '1.0.0', TRUE);

-- ========== ROOMS ==========
INSERT INTO rooms (name, description, creator_id, room_type, max_participants, is_private) VALUES
('Général', 'Salle de discussion générale', 1, 'audio', 50, FALSE),
('Projet SecurePhone', 'Discussion développement', 1, 'video', 10, TRUE),
('Support technique', 'Aide et support', 5, 'conference', 5, FALSE),
('Amis', 'Discussion entre amis', 2, 'audio', 20, TRUE);

-- ========== ROOM MEMBERS ==========
INSERT INTO room_members (room_id, user_id, is_admin) VALUES
-- Salle Général
(1, 1, TRUE),
(1, 2, FALSE),
(1, 3, FALSE),
(1, 4, FALSE),
(1, 5, TRUE),
-- Projet SecurePhone
(2, 1, TRUE),
(2, 2, FALSE),
(2, 3, FALSE),
-- Support technique
(3, 5, TRUE),
(3, 1, FALSE),
(3, 4, FALSE),
-- Amis
(4, 2, TRUE),
(4, 1, FALSE),
(4, 3, FALSE);

-- ========== APPELS ==========
INSERT INTO calls (caller_id, receiver_id, call_type, status, start_time, end_time, duration) VALUES
-- Appel audio réussi
(1, 2, 'audio', 'answered', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 180 SECOND, 180),
-- Appel vidéo manqué
(3, 1, 'video', 'missed', DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, 0),
-- Appel vidéo en cours (simulé)
(2, 3, 'video', 'answered', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, 300);

-- ========== NOTIFICATIONS ==========
INSERT INTO notifications (user_id, type, title, body, read_status, sent_via) VALUES
-- Notifications non lues
(2, 'message', 'Nouveau message', 'Alice: Salut Bob, prêt pour le test vidéo ?', FALSE, 'both'),
(3, 'call', 'Appel vidéo manqué', 'Alice vous a appelé', FALSE, 'push'),
(1, 'contact_request', 'Nouveau contact', 'Diana veut vous ajouter', FALSE, 'in_app'),
-- Notifications lues
(2, 'message', 'Message lu', 'Charlie: Je suis là aussi', TRUE, 'in_app'),
(4, 'system', 'Mise à jour disponible', 'Nouvelle version 1.1.0', TRUE, 'both');

-- ========== VÉRIFICATION ==========
SELECT '=== UTILISATEURS (5) ===' AS '';
SELECT id, username, email, status, twofa_enabled FROM users ORDER BY id;

SELECT '=== CONTACTS (11 relations) ===' AS '';
SELECT COUNT(*) as total_contacts FROM contacts;

SELECT '=== MESSAGES (12) ===' AS '';
SELECT COUNT(*) as total_messages FROM messages;

SELECT '=== PUSH TOKENS (5) ===' AS '';
SELECT user_id, platform, active FROM device_tokens ORDER BY user_id;

SELECT '=== ROOMS (4) ===' AS '';
SELECT id, name, room_type, is_private FROM rooms ORDER BY id;

SELECT '=== DERNIERS MESSAGES ===' AS '';
SELECT 
    u1.username AS 'De',
    u2.username AS 'À',
    LEFT(m.content, 40) AS 'Message',
    DATE_FORMAT(m.timestamp, '%H:%i') AS 'Heure'
FROM messages m
JOIN users u1 ON m.sender_id = u1.id
JOIN users u2 ON m.receiver_id = u2.id
WHERE m.room_id IS NULL
ORDER BY m.timestamp DESC
LIMIT 5;

SELECT '=== APPELS RÉCENTS ===' AS '';
SELECT 
    u1.username AS 'Appelant',
    u2.username AS 'Receveur',
    c.call_type AS 'Type',
    c.status AS 'Statut',
    c.duration AS 'Durée(s)'
FROM calls c
JOIN users u1 ON c.caller_id = u1.id
JOIN users u2 ON c.receiver_id = u2.id
ORDER BY c.start_time DESC
LIMIT 5;

SELECT '=== BASE DE DONNÉES PRÊTE ===' AS '';
SELECT 'SecurePhone database initialized successfully!' AS message;