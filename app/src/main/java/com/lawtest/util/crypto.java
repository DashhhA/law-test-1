package com.lawtest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

// функции, связанные с криптографией
public class crypto {
    // класс, содержащий в себе пароль и соль
    // здесь пароль - результат работы sha-256
    public static class PassSalt {
        public byte[] salt;
        public byte[] pass;

        PassSalt(byte[] pass, byte[] salt){
            this.pass = pass;
            this.salt = salt;
        }
    }

    // преобразование строки в пароль и соль
    public static PassSalt getPassSalt(String pass) {
        byte[] salt = new byte[256];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        return getPassSalt(pass, salt);
    }

    // проверка, можно ли из данной строки получить переденный пароль
    public static boolean checkPass(String password, byte[] pass, byte[] salt){
        PassSalt passSalt = new PassSalt(pass, salt);
        return checkPass(password, passSalt);
    }

    // преобразование строки в пароль с заданной солью
    public static String getPassBySalt(String password, byte[] salt){
        PassSalt passSalt = getPassSalt(password, salt);
        if (passSalt != null) return new String(passSalt.pass);
        return null;
    }

    // проверка, можно ли из данной строки получить переденный пароль
    private static boolean checkPass(String pass, PassSalt passSalt){
        byte[] salted = concatBytes(pass.getBytes(), passSalt.salt);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(salted);
            return Arrays.equals(digest, passSalt.pass);
        }catch (NoSuchAlgorithmException e) {}

        return false;
    }

    // преобразование строки в пароль с заданной солью
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

    // слияние двух бассивов byte[]
    private static byte[] concatBytes(byte[] start, byte[] end){
        byte[] ans = new byte[start.length + end.length];
        System.arraycopy(start, 0, ans, 0, start.length);
        System.arraycopy(end, 0, ans, start.length, end.length);
        return ans;
    }
}
