package Server;

import java.sql.*;

public class LeaveBalanceRepository {

    public int getYearBalanceOrCreate(String employeeId, int year, int defaultBalance) throws SQLException {
        String check = "SELECT BALANCE FROM LEAVE_BALANCE WHERE EMPLOYEE_ID = ? AND YEAR = ?";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {

            ps.setString(1, employeeId);
            ps.setInt(2, year);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("BALANCE");
            }
        }

        String insert = "INSERT INTO LEAVE_BALANCE (EMPLOYEE_ID, YEAR, BALANCE) VALUES (?, ?, ?)";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(insert)) {

            ps.setString(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, defaultBalance);

            ps.executeUpdate();
        }

        return defaultBalance;
    }

    public void setBalance(String employeeId, int year, int newBalance) throws SQLException {
        String sql = "UPDATE LEAVE_BALANCE SET BALANCE = ? WHERE EMPLOYEE_ID = ? AND YEAR = ?";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, newBalance);
            ps.setString(2, employeeId);
            ps.setInt(3, year);

            ps.executeUpdate();
        }
    }

    public boolean hasRow(String employeeId, int year) throws SQLException {
        String sql = "SELECT 1 FROM LEAVE_BALANCE WHERE EMPLOYEE_ID = ? AND YEAR = ?";
        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
}