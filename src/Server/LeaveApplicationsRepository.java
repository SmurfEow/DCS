package Server;

import java.sql.*;
import java.time.LocalDate;

public class LeaveApplicationsRepository {

    public int apply(String employeeId, String leaveType, Date startDate, Date endDate,
                     int daysRequested, String reason) throws SQLException {

        String sql = "INSERT INTO LEAVE_APPLICATIONS " +
                     "(EMPLOYEE_ID, LEAVE_TYPE, START_DATE, END_DATE, DAYS_REQUESTED, REASON, STATUS) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, employeeId);
            ps.setString(2, leaveType);
            ps.setDate(3, startDate);
            ps.setDate(4, endDate);
            ps.setInt(5, daysRequested);
            ps.setString(6, reason);

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void decide(int leaveId, String decidedBy, String status) throws SQLException {
        String sql = "UPDATE LEAVE_APPLICATIONS SET STATUS = ?, DECIDED_BY = ?, DECIDED_AT = CURRENT_TIMESTAMP WHERE LEAVE_ID = ?";
        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, decidedBy);
            ps.setInt(3, leaveId);
            ps.executeUpdate();
        }
    }

    public LeaveRow find(int leaveId) throws SQLException {
        String sql = "SELECT LEAVE_ID, EMPLOYEE_ID, LEAVE_TYPE, START_DATE, END_DATE, DAYS_REQUESTED, STATUS " +
                     "FROM LEAVE_APPLICATIONS WHERE LEAVE_ID = ?";
        try (Connection c = DatabaseSocket.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, leaveId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            return new LeaveRow(
                rs.getInt("LEAVE_ID"),
                rs.getString("EMPLOYEE_ID"),
                rs.getString("LEAVE_TYPE"),
                rs.getDate("START_DATE"),
                rs.getDate("END_DATE"),
                rs.getInt("DAYS_REQUESTED"),
                rs.getString("STATUS")
            );
        }
    }

    public static class LeaveRow {
        public final int leaveId;
        public final String employeeId;
        public final String leaveType;
        public final Date startDate;
        public final Date endDate;
        public final int daysRequested;
        public final String status;

        public LeaveRow(int leaveId, String employeeId, String leaveType, Date startDate, Date endDate, int daysRequested, String status) {
            this.leaveId = leaveId;
            this.employeeId = employeeId;
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.daysRequested = daysRequested;
            this.status = status;
        }
    }
}
