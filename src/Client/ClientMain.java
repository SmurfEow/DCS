/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
//git remote add origin https://github.com/SmurfEow/DCS.git
//git branch -M main
//git push -u origin main

package Client;

import Common.Authorization;
import Common.UserSession;
import java.rmi.Naming;
import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) throws Exception {

        String serverIP = "192.168.1.5";

        Authorization service = (Authorization)
                Naming.lookup("rmi://" + serverIP + ":1099/Authorization");

        System.out.println("Connected: " + service.ping());

        Scanner sc = new Scanner(System.in);

        System.out.print("User ID: ");
        String id = sc.nextLine();

        System.out.print("Password: ");
        String pw = sc.nextLine();

        UserSession session = service.login(id, pw);

        System.out.println("Login success. Role: " + session.getRole());

        service.logout(session);
        System.out.println("Logged out.");
    }
}
    

