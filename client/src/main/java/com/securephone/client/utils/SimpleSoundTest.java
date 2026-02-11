package com.securephone.client.utils;

/**
 * Test simple pour vérifier la lecture des sons MP3
 */
public class SimpleSoundTest {
    public static void main(String[] args) {
        System.out.println("=== Test SoundPlayer avec MP3 ===\n");
        
        // Récupérer l'instance du SoundPlayer
        SoundPlayer player = SoundPlayer.getInstance();
        
        System.out.println("\nTest de lecture des sons:");
        System.out.println("------------------------");
        
        // Tester quelques sons
        System.out.println("\n1. Lecture son MESSAGE_RECEIVED...");
        player.playMessageReceived();
        
        try {
            Thread.sleep(2000); // Attendre que le son se termine
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n2. Lecture son CALL_INCOMING...");
        player.playCallIncoming();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n3. Lecture son ERROR...");
        player.playError();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n✓ Tests terminés!");
        player.dispose();
    }
}
