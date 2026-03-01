/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.Naming;

public class ServerSocket {
    public static void main(String[] args) {
        try {
            // ✅ SSL registry
            Registry reg = LocateRegistry.createRegistry(
                1099,
                new SslRMIClientSocketFactory(),
                new SslRMIServerSocketFactory()
            );

            // ✅ bind service (ServiceImpl must also be SSL-exported, see next section)
            ServiceImpl svc = new ServiceImpl();
            Naming.rebind("rmi://0.0.0.0:1099/Authorization", svc);

            System.out.println("✅ SSL-RMI Server started on port 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
