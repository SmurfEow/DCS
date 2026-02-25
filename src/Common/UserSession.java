/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

import java.io.Serializable;

public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String tokenSession;
    private final UserRole role;
    private final String userId;
    
    public UserSession(String tokenSession, UserRole role, String userId){
        this.tokenSession = tokenSession;
        this.role = role;
        this.userId = userId;
    }
    //getter(s)
    public String getTokenSession(){
        return tokenSession;
    }
    public UserRole getRole(){
        return role;
    }
    public String getUserId(){
        return userId;
    }
}
