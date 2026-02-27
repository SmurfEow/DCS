/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import Common.Authorization;

public class ServerSocket {
    public static void main(String[] args) throws Exception {

        String serverIP = "192.168.1.5"; // <-- server laptop IP

        System.setProperty("java.rmi.server.hostname", serverIP);

        LocateRegistry.createRegistry(1099);
        
        Database.init();

        Authorization service = new ServiceImpl();

        Naming.rebind("rmi://" + serverIP + ":1099/Authorization", service);

        System.out.println("Server started at " + serverIP + ":1099");
    }
}
