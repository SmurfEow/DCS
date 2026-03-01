package Server;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class ServerSocket {

    // ✅ EDIT THIS to the real absolute path on PC A
    private static final String KEYSTORE_PATH = "C:\\Users\\User\\Documents\\NetBeansProjects\\Crest\\server.keystore";
    private static final String KEYSTORE_PASS = "888888";

    // ✅ PC A IP (your server PC)
    private static final String SERVER_IP = "192.168.1.19";

    public static void main(String[] args) {
        try {
            // ------------------------------------------------------------
            // 1) Force SSL keystore/truststore (prevents DefaultSSLContext crash)
            // ------------------------------------------------------------
            System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
            System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);
            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

            System.setProperty("javax.net.ssl.trustStore", KEYSTORE_PATH);
            System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASS);
            System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

            // ------------------------------------------------------------
            // 2) Ensure stubs point to PC A IP (important for multi-PC)
            // ------------------------------------------------------------
            System.setProperty("java.rmi.server.hostname", SERVER_IP);

            // ------------------------------------------------------------
            // 3) Sanity prints (so you can see if path is wrong)
            // ------------------------------------------------------------
            System.out.println("=== SSL SETTINGS ===");
            System.out.println("keyStore   = " + System.getProperty("javax.net.ssl.keyStore"));
            System.out.println("trustStore = " + System.getProperty("javax.net.ssl.trustStore"));
            System.out.println("exists?    = " + new File(KEYSTORE_PATH).exists());
            System.out.println("hostname   = " + System.getProperty("java.rmi.server.hostname"));
            System.out.println("====================");

            if (!new File(KEYSTORE_PATH).exists()) {
                throw new IllegalStateException(
                    "Keystore NOT FOUND at: " + KEYSTORE_PATH +
                    "\nFix KEYSTORE_PATH to the real location of server.keystore on PC A."
                );
            }

            // ------------------------------------------------------------
            // 4) Create SSL RMI registry and bind SSL-exported service
            // ------------------------------------------------------------
            var csf = new SslRMIClientSocketFactory();
            var ssf = new SslRMIServerSocketFactory();

            Registry reg = LocateRegistry.createRegistry(1099, csf, ssf);

            // ServiceImpl already exports itself with SSL in its constructor:
            // super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
            ServiceImpl svc = new ServiceImpl();

            reg.rebind("Authorization", svc);

            System.out.println("✅ SSL-RMI Server started on port 1099");
            System.out.println("✅ Bound name: Authorization");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}