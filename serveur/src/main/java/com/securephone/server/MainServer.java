package com.securephone.server;

import com.securephone.server.database.DatabaseManager;
import com.securephone.server.network.PacketRouter;
import com.securephone.server.network.SocketManager;
import com.securephone.server.udp.AudioServer;
import com.securephone.server.udp.VideoServer;

public class MainServer {
    
    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE SERVEUR SECUREPHONE ===");
        
        try {
            // Initialiser la base de données
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                System.out.println("[MAIN] Base de données initialisée");
            } catch (Exception e) {
                System.out.println("[MAIN] Base de données indisponible, mode sans DB");
            }

            // Démarrer le serveur TCP chat
            PacketRouter router = new PacketRouter();
            SocketManager socketManager = new SocketManager(router);
            socketManager.start();
            System.out.println("[MAIN] Serveur chat TCP démarré sur le port 8081");
            
            // Démarrer le serveur UDP Audio
            AudioServer audioServer = new AudioServer();
            audioServer.start();
            System.out.println("[MAIN] Serveur Audio UDP démarré sur le port 50000");
            
            // Démarrer le serveur UDP Vidéo
            VideoServer videoServer = new VideoServer();
            videoServer.start();
            System.out.println("[MAIN] Serveur Vidéo UDP démarré sur le port 50020");
            
            System.out.println("\n✅ Serveur SecurePhone prêt !");
            System.out.println("API REST: http://localhost:8080/securephone/api/");
            System.out.println("Chat TCP: localhost:8081");
            System.out.println("\nAppuyez sur Ctrl+C pour arrêter...");
            
            // Garder le programme actif
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur démarrage serveur: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}