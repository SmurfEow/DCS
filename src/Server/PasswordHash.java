/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHash {
    private static final SecureRandom rng = new SecureRandom();

    public static String newSalt() {
        byte[] salt = new byte[16];
        rng.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String saltB64, String password) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltB64);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] digest = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verification(String saltB64, String expectedHashB64, String password) {
        return hash(saltB64, password).equals(expectedHashB64);
    }

    private PasswordHash() {}
}
