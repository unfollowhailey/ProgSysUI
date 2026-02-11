package com.securephone.server.database;

import java.sql.*;

public class DeviceTokenDAO {
    
    private final Connection connection;
    
    public DeviceTokenDAO(Connection connection) {
        this.connection = connection;
    }
    
    public boolean saveToken(int userId, String deviceToken, String platform) throws SQLException {
        String sql = "INSERT INTO device_tokens (user_id, device_token, platform) "
            + "VALUES (?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "last_used = NOW(), "
            + "active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, deviceToken);
            stmt.setString(3, platform);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public String getDeviceToken(int userId) throws SQLException {
        String sql = "SELECT device_token FROM device_tokens "
            + "WHERE user_id = ? AND active = TRUE "
            + "ORDER BY last_used DESC "
            + "LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("device_token");
            }
            return null;
        }
    }
    
    public boolean deactivateToken(String deviceToken) throws SQLException {
        String sql = "UPDATE device_tokens SET active = FALSE WHERE device_token = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, deviceToken);
            return stmt.executeUpdate() > 0;
        }
    }
}