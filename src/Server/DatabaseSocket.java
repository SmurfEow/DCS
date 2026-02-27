/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseSocket {

    // Embedded Derby (no DB server needed)
    private static final String URL = "jdbc:derby:crestDB;create=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
