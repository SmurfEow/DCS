/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Authorization extends Remote {

    String ping() throws RemoteException;

    UserSession login(String userId, String password) throws RemoteException;
    void logout(UserSession session) throws RemoteException;

    // HR
    Employee registerEmployee(UserSession session, String firstName, String lastName, String icPassport, String initPass)
            throws RemoteException;

    // Staff
    Employee updateDetails(UserSession session, Employee updated) throws RemoteException;
    int leaveBalance(UserSession session) throws RemoteException;
}

