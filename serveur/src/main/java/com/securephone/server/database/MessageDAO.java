package com.securephone.server.database;

import com.securephone.server.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    
    private final Connection connection;
    
    public MessageDAO(Connection connection) {
        this.connection = connection;
    }
    
    public boolean saveMessage(Message message) throws SQLException {
        String sql = "INSERT INTO messages "
            + "(sender_id, receiver_id, room_id, message_type, content, encrypted) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getRoomId());
            stmt.setString(4, message.getMessageType());
            stmt.setString(5, message.getContent());
            stmt.setBoolean(6, message.isEncrypted());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Message> getConversation(int user1Id, int user2Id, int limit) throws SQLException {
        String sql = "SELECT * FROM messages "
            + "WHERE (sender_id = ? AND receiver_id = ?) "
            + "OR (sender_id = ? AND receiver_id = ?) "
            + "ORDER BY timestamp DESC "
            + "LIMIT ?";
        
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            stmt.setInt(5, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }
    
    public List<Message> getUnreadMessages(int userId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE receiver_id = ? AND read_status = FALSE";
        
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }
    
    public boolean markAsRead(int messageId) throws SQLException {
        String sql = "UPDATE messages SET read_status = TRUE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setRoomId(rs.getString("room_id"));
        message.setMessageType(rs.getString("message_type"));
        message.setContent(rs.getString("content"));
        message.setEncrypted(rs.getBoolean("encrypted"));
        message.setTimestamp(rs.getTimestamp("timestamp"));
        message.setReadStatus(rs.getBoolean("read_status"));
        return message;
    }
}