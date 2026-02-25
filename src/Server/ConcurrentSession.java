/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Common.UserRole;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSession {

    public static class cred {
        public final String salt;
        public final String hash;
        public final UserRole role;

        public cred(String salt, String hash, UserRole role) {
            this.salt = salt;
            this.hash = hash;
            this.role = role;
        }
    }

    private final ConcurrentHashMap<String, cred> map = new ConcurrentHashMap<>();

    public void put(String userId, cred credential) {
        map.put(userId, credential);
    }

    public cred get(String userId) {
        return map.get(userId);
    }

    public boolean exists(String userId) {
        return map.containsKey(userId);
    }
}
