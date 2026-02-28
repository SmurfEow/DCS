/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseSocket {

    private static final String URL = "jdbc:derby:crestDB;create=true";

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Derby driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}

//package Server;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DatabaseSocket {
//
//    // ✅ Device 3 IP here
//    private static final String DB_HOST = "192.168.1.XX"; // <-- DB Server IP
//    private static final int DB_PORT = 1527;
//    private static final String DB_NAME = "crestDB";
//
//    // Network JDBC URL
//    private static final String URL =
//            "jdbc:derby://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + ";create=true";
//
//    static {
//        try {
//            // ✅ Network client driver
//            Class.forName("org.apache.derby.jdbc.ClientDriver");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("Derby ClientDriver not found. Add derbyclient.jar", e);
//        }
//    }
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL);
//    }
//}
