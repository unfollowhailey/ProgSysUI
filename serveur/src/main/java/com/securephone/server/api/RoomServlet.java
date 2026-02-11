package com.securephone.server.api;

import com.securephone.server.database.DatabaseManager;
import com.securephone.server.security.SessionManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/api/rooms/*")
public class RoomServlet extends HttpServlet {

    private DatabaseManager dbManager;

    @Override
    public void init() {
        dbManager = DatabaseManager.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetRooms(req, resp);
            } else if ("/members".equals(pathInfo)) {
                handleGetRoomMembers(req, resp);
            } else {
                sendError(resp, 404, "Endpoint non trouvé");
            }
        } catch (Exception e) {
            sendError(resp, 500, "Erreur interne: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if ("/create".equals(pathInfo)) {
                handleCreateRoom(req, resp);
            } else if ("/join".equals(pathInfo)) {
                handleJoinRoom(req, resp);
            } else if ("/leave".equals(pathInfo)) {
                handleLeaveRoom(req, resp);
            } else {
                sendError(resp, 404, "Endpoint non trouvé");
            }
        } catch (Exception e) {
            sendError(resp, 500, "Erreur interne: " + e.getMessage());
        }
    }

    private void handleGetRooms(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String sessionId = req.getParameter("session_id");
        SessionManager.Session session = SessionManager.getSession(sessionId);

        if (session == null) {
            sendError(resp, 401, "Session invalide");
            return;
        }

        // Récupérer les salles de l'utilisateur
        String sql = "SELECT r.* FROM rooms r "
            + "JOIN room_members rm ON r.id = rm.room_id "
            + "WHERE rm.user_id = ? "
            + "ORDER BY r.created_at DESC";
        try (var stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, session.userId);
            var rs = stmt.executeQuery();

            JSONArray roomsArray = new JSONArray();
            while (rs.next()) {
                JSONObject room = new JSONObject();
                room.put("id", rs.getInt("id"));
                room.put("name", rs.getString("name"));
                room.put("type", rs.getString("room_type"));
                room.put("private", rs.getBoolean("is_private"));
                room.put("created_at", rs.getTimestamp("created_at").toString());
                roomsArray.put(room);
            }

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("rooms", roomsArray);

            resp.getWriter().write(response.toString());
        }
    }

    private void handleCreateRoom(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String sessionId = req.getParameter("session_id");
        SessionManager.Session session = SessionManager.getSession(sessionId);

        if (session == null) {
            sendError(resp, 401, "Session invalide");
            return;
        }

        String name = req.getParameter("name");
        String type = req.getParameter("type");
        boolean isPrivate = Boolean.parseBoolean(req.getParameter("is_private"));

        if (name == null || name.trim().isEmpty()) {
            sendError(resp, 400, "Nom de la salle requis");
            return;
        }

        // Créer la salle
        String sql = "INSERT INTO rooms (name, creator_id, room_type, is_private) VALUES (?, ?, ?, ?)";
        try (var stmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, session.userId);
            stmt.setString(3, type != null ? type : "audio");
            stmt.setBoolean(4, isPrivate);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                sendError(resp, 500, "Erreur création salle");
                return;
            }

            // Récupérer l'ID généré
            var keys = stmt.getGeneratedKeys();
            int roomId = -1;
            if (keys.next()) {
                roomId = keys.getInt(1);
            }

            // Ajouter le créateur comme membre admin
            String memberSql = "INSERT INTO room_members (room_id, user_id, is_admin) VALUES (?, ?, TRUE)";
            try (var memberStmt = dbManager.getConnection().prepareStatement(memberSql)) {
                memberStmt.setInt(1, roomId);
                memberStmt.setInt(2, session.userId);
                memberStmt.executeUpdate();
            }

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("room_id", roomId);
            response.put("message", "Salle créée avec succès");

            resp.getWriter().write(response.toString());
        }
    }

    private void handleJoinRoom(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String sessionId = req.getParameter("session_id");
        SessionManager.Session session = SessionManager.getSession(sessionId);

        if (session == null) {
            sendError(resp, 401, "Session invalide");
            return;
        }

        int roomId = Integer.parseInt(req.getParameter("room_id"));

        // Vérifier si la salle existe
        String checkSql = "SELECT 1 FROM rooms WHERE id = ?";
        try (var checkStmt = dbManager.getConnection().prepareStatement(checkSql)) {
            checkStmt.setInt(1, roomId);
            var rs = checkStmt.executeQuery();
            if (!rs.next()) {
                sendError(resp, 404, "Salle non trouvée");
                return;
            }
        }

        // Rejoindre la salle
        String sql = "INSERT IGNORE INTO room_members (room_id, user_id) VALUES (?, ?)";
        try (var stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, session.userId);
            int affected = stmt.executeUpdate();

            JSONObject response = new JSONObject();
            if (affected > 0) {
                response.put("status", "success");
                response.put("message", "Salle rejointe");
            } else {
                response.put("status", "info");
                response.put("message", "Déjà membre de la salle");
            }

            resp.getWriter().write(response.toString());
        }
    }

    private void handleGetRoomMembers(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String sessionId = req.getParameter("session_id");
        SessionManager.Session session = SessionManager.getSession(sessionId);

        if (session == null) {
            sendError(resp, 401, "Session invalide");
            return;
        }

        int roomId = Integer.parseInt(req.getParameter("room_id"));

        // Récupérer les membres
        String sql = "SELECT u.id, u.username, u.status, rm.is_admin, rm.joined_at "
            + "FROM room_members rm "
            + "JOIN users u ON rm.user_id = u.id "
            + "WHERE rm.room_id = ? "
            + "ORDER BY rm.joined_at";
        try (var stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            var rs = stmt.executeQuery();

            JSONArray membersArray = new JSONArray();
            while (rs.next()) {
                JSONObject member = new JSONObject();
                member.put("id", rs.getInt("id"));
                member.put("username", rs.getString("username"));
                member.put("status", rs.getString("status"));
                member.put("is_admin", rs.getBoolean("is_admin"));
                member.put("joined_at", rs.getTimestamp("joined_at").toString());
                membersArray.put(member);
            }

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("members", membersArray);

            resp.getWriter().write(response.toString());
        }
    }

    private void handleLeaveRoom(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String sessionId = req.getParameter("session_id");
        SessionManager.Session session = SessionManager.getSession(sessionId);

        if (session == null) {
            sendError(resp, 401, "Session invalide");
            return;
        }

        int roomId = Integer.parseInt(req.getParameter("room_id"));

        String sql = "DELETE FROM room_members WHERE room_id = ? AND user_id = ?";
        try (var stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, session.userId);
            int affected = stmt.executeUpdate();

            JSONObject response = new JSONObject();
            response.put("status", affected > 0 ? "success" : "info");
            response.put("message", affected > 0 ? "Salle quittée" : "Non membre de cette salle");

            resp.getWriter().write(response.toString());
        }
    }

    private void sendError(HttpServletResponse resp, int code, String message)
            throws IOException {

        resp.setStatus(code);
        JSONObject error = new JSONObject();
        error.put("error", message);
        resp.getWriter().write(error.toString());
    }
}
