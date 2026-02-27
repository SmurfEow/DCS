package Server;

import Common.Employee;
import java.sql.*;

public class EmployeesRepository {

    public void insertBasic(Employee e) throws SQLException {
        String sql = "INSERT INTO EMPLOYEES (EMPLOYEE_ID, FIRST_NAME, LAST_NAME, IC_PASSPORT) VALUES (?, ?, ?, ?)";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getEmployeeId());
            ps.setString(2, e.getFirstName());
            ps.setString(3, e.getLastName());
            ps.setString(4, e.getIcPassport());

            ps.executeUpdate();
        }
    }

    public Employee findBasic(String employeeId) throws SQLException {
        String sql = "SELECT EMPLOYEE_ID, FIRST_NAME, LAST_NAME, IC_PASSPORT FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Employee e = new Employee(
                    rs.getString("FIRST_NAME"),
                    rs.getString("LAST_NAME"),
                    rs.getString("IC_PASSPORT")
            );
            e.setEmployeeId(rs.getString("EMPLOYEE_ID"));
            return e;
        }
    }

    public boolean icPassportExists(String icPassport) throws SQLException {
        String sql = "SELECT 1 FROM EMPLOYEES WHERE IC_PASSPORT = ?";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, icPassport);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
}