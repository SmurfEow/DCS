/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IdGenerator {

    private static int nextFromDb(String table, String idColumn, String prefix) {
        String sql = "SELECT MAX(" + idColumn + ") AS MAX_ID FROM " + table +
                     " WHERE " + idColumn + " LIKE ?";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, prefix + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                int next = 1;

                if (rs.next()) {
                    String maxId = rs.getString("MAX_ID"); 
                    if (maxId != null && maxId.startsWith(prefix + "-")) {
                        String numPart = maxId.substring((prefix + "-").length());
                        int num = Integer.parseInt(numPart);
                        next = num + 1;
                    }
                }

                return next;
            }

        } catch (Exception e) {
            // If DB fails, fallback to 1 (but log it so you know)
            System.out.println("IdGenerator DB error: " + e.getMessage());
            return 1;
        }
    }

    public static String nextEmployeeCounter() {
        int n = nextFromDb("EMPLOYEES", "EMPLOYEE_ID", "E");
        return String.format("E-%06d", n);
    }

    public static String nextHumanResourceCounter() {
        int n = nextFromDb("USERS", "USER_ID", "H");
        return String.format("H-%06d", n);
    }
}
