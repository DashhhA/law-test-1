package com.lawtest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class crypto {
    public static class PassSalt {
        public byte[] salt;
        public byte[] pass;

        PassSalt(byte[] pass, byte[] salt){
            this.pass = pass;
            this.salt = salt;
        }
    }
    public static PassSalt getPassSalt(String pass) {
        byte[] salt = new byte[256];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        return getPassSalt(pass, salt);
    }

    public static boolean checkPass(String password, byte[] pass, byte[] salt){
        PassSalt passSalt = new PassSalt(pass, salt);
        return checkPass(password, passSalt);
    }

    public static String getPassBySalt(String password, byte[] salt){
        PassSalt passSalt = getPassSalt(password, salt);
        if (passSalt != null) return new String(passSalt.pass);
        return null;
    }

    private static boolean checkPass(String pass, PassSalt passSalt){
        byte[] salted = concatBytes(pass.getBytes(), passSalt.salt);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(salted);
            return Arrays.equals(digest, passSalt.pass);
        }catch (NoSuchAlgorithmException e) {}

        return false;
    }

    private static PassSalt getPassSalt(String pass, byte[] salt) {
        byte[] plaintext = pass.getBytes();
        byte[] salted = concatBytes(plaintext, salt);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(salted);
            return new PassSalt(digest, salt);
        }catch (NoSuchAlgorithmException e) {}

        return null;
    }

    private static byte[] concatBytes(byte[] start, byte[] end){
        byte[] ans = new byte[start.length + end.length];
        System.arraycopy(start, 0, ans, 0, start.length);
        System.arraycopy(end, 0, ans, start.length, end.length);
        return ans;
    }
}
