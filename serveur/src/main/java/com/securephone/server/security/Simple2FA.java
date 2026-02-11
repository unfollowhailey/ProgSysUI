package com.securephone.server.security;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Simple2FA {
    
    private static final ConcurrentHashMap<String, TwoFACode> codes = 
        new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    static class TwoFACode {
        String code;
        long timestamp;
        
        TwoFACode(String code) {
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 300000; // 5 minutes
        }
    }
    
    public static String generateCode(String username) {
        String code = String.format("%06d", random.nextInt(1000000));
        codes.put(username, new TwoFACode(code));
        
        System.out.println("\n[2FA] Code pour " + username + ": " + code);
        System.out.println("Valide 5 minutes\n");
        
        return code;
    }
    
    public static boolean verifyCode(String username, String inputCode) {
        TwoFACode twoFACode = codes.get(username);
        if (twoFACode == null || twoFACode.isExpired()) {
            codes.remove(username);
            return false;
        }
        boolean valid = twoFACode.code.equals(inputCode);
        if (valid) {
            codes.remove(username);
        }
        return valid;
    }
}