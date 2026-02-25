/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Common.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
  private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
  
  public UserSession create(UserRole role, String userId) {
        String token = UUID.randomUUID().toString();
        UserSession session = new UserSession(token, role, userId);
        sessions.put(token, session);
        return session;
    }
  public UserSession require(UserSession session){
      if (session == null) throw new SecurityException("No Session found!");
      UserSession none = sessions.get(session.getTokenSession());
      if (none == null) throw new SecurityException("Session expired! Please reload & try again.");
      return none;
  }
  public void remove(UserSession session){
      if(session != null) sessions.remove(session.getTokenSession());
  }
}
