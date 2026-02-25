/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;
import Common.RemoteTesting;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
/**
 *
 * @author User
 */
public class ServiceImpl extends UnicastRemoteObject implements RemoteTesting{
    protected ServiceImpl()throws RemoteException{
        super();
    }
    @Override
    public String ping() throws RemoteException{
        System.out.println("Client ping..");
        return "Server is valid!";
    }
}
