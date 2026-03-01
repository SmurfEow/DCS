package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class ServerSocket {
    public static void main(String[] args) {
        try {
            // Force hostname so client gets correct stub (PC A IP)
            System.setProperty("java.rmi.server.hostname", "192.168.1.19");

            var csf = new SslRMIClientSocketFactory();
            var ssf = new SslRMIServerSocketFactory();

            // 1) SSL registry
            Registry reg = LocateRegistry.createRegistry(1099, csf, ssf);

            // 2) ServiceImpl is already SSL-exported (because it extends UnicastRemoteObject)
            ServiceImpl svc = new ServiceImpl();

            // 3) Bind using registry (don’t use Naming + 0.0.0.0)
            reg.rebind("Authorization", svc);

            System.out.println("✅ SSL-RMI Server started on port 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}