package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class ServerSocket {

    private static final String SERVER_IP = "192.168.1.19";
    private static final String KEYSTORE_PATH =
            "C:\\Users\\User\\Documents\\NetBeansProjects\\Crest\\server.keystore";
    private static final String KEYSTORE_PASS = "888888";

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", SERVER_IP);

            // ✅ server identity (private key)
            System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
            System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);
            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

            // ✅ Important: DO NOT set trustStore on server unless you need mutual TLS
            System.clearProperty("javax.net.ssl.trustStore");
            System.clearProperty("javax.net.ssl.trustStorePassword");

            System.out.println("java.version=" + System.getProperty("java.version"));
            System.out.println("java.home=" + System.getProperty("java.home"));
            System.out.println("keyStore=" + System.getProperty("javax.net.ssl.keyStore"));

            var csf = new SslRMIClientSocketFactory();
            var ssf = new SslRMIServerSocketFactory(null, null, false); // ✅ no client auth

            Registry reg = LocateRegistry.createRegistry(1099, csf, ssf);

            ServiceImpl svc = new ServiceImpl();
            reg.rebind("Authorization", svc);

            System.out.println("✅ SSL-RMI Server started on port 1099");

        } catch (Exception e) {
            System.out.println("❌ Server failed: " + e);
            e.printStackTrace();
        }
    }
}