package Server;

import Common.Authorization;
import Common.Employee;
import Common.UserRole;
import Common.UserSession;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Year;

public class ServiceImpl extends UnicastRemoteObject implements Authorization {

    private final Session sessionManager = new Session();
    private final ActivityLog audit = new ActivityLog("audit.log");

    private final UsersRepository users = new UsersRepository();
    private final EmployeesRepository employees = new EmployeesRepository();
    private final EmployeeDetailsRepository details = new EmployeeDetailsRepository();
    private final LeaveBalanceRepository leaveBalanceRepo = new LeaveBalanceRepository();
    private final LeaveApplicationsRepository leaveRepo = new LeaveApplicationsRepository();
    protected ServiceImpl() throws RemoteException {
        super();

        // Create default HR only ONCE
        try {
            String hrId = "H-000001";
            if (!users.exists(hrId)) {
                String salt = PasswordHash.newSalt();
                String hash = PasswordHash.hash(salt, "hr123");
                users.insert(hrId, UserRole.HR, salt, hash);
                audit.log("Default HR created: " + hrId);
                System.out.println("Default HR created: " + hrId + " pw=hr123");
            } else {
                System.out.println("Default HR exists: H-000001");
            }
        } catch (Exception e) {
            System.out.println("HR bootstrap error: " + e.getMessage());
        }
    }

    @Override
    public String ping() throws RemoteException {
        audit.log("ping()");
        return "Server is valid!";
    }

    @Override
    public UserSession login(String userId, String password) throws RemoteException {
        try {
            UsersRepository.UserRow u = users.find(userId);
            if (u == null) {
                audit.log("login fail no user: " + userId);
                throw new SecurityException("Invalid credentials");
            }

            if (!PasswordHash.verification(u.salt, u.hash, password)) {
                audit.log("login fail bad pw: " + userId);
                throw new SecurityException("Invalid credentials");
            }

            UserSession s = sessionManager.create(u.role, userId);
            audit.log("login ok: " + userId + " role=" + u.role);
            return s;

        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new RemoteException("DB error during login", e);
        }
    }

    @Override
    public void logout(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        sessionManager.remove(session);
        audit.log("logout: " + s.getUserId()); // ✅ fixed
    }

    @Override
    public Employee registerEmployee(UserSession session, String firstName, String lastName, String icPassport, String initPass)
            throws RemoteException {

        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.HR) throw new SecurityException("HR only");

        try {
            // generate employee ID (your existing generator)
            String empId = IdGenerator.nextEmployeeCounter();

            // create employee record
            Employee e = new Employee(firstName, lastName, icPassport);
            e.setEmployeeId(empId);

            employees.insertBasic(e);               // writes EMPLOYEES
            details.createIfNotExists(empId);       // creates EMPLOYEE_DETAILS row (optional)

            // create login
            String salt = PasswordHash.newSalt();
            String hash = PasswordHash.hash(salt, initPass);
            users.insert(empId, UserRole.STAFF, salt, hash);

            // init leave balance for current year (LEAVE_BALANCE table)
            int year = Year.now().getValue();
            leaveBalanceRepo.getYearBalanceOrCreate(empId, year, 15);

            audit.log("HR " + s.getUserId() + " registered " + empId); // ✅ fixed
            return e;

        } catch (Exception e) {
            throw new RemoteException("DB error during registerEmployee", e);
        }
    }

    @Override
    public Employee updateDetails(UserSession session, Employee updated) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");
        if (updated == null) throw new IllegalArgumentException("Missing employee data");

        // ✅ UserSession has getUserId()
        if (!s.getUserId().equals(updated.getEmployeeId()))
            throw new SecurityException("Cannot update other users");

        try {
            Employee basic = employees.findBasic(s.getUserId());
            if (basic == null) throw new IllegalStateException("Employee not found");

            details.updateFromEmployee(updated);

            audit.log("Staff " + s.getUserId() + " updated details");
            return updated;

        } catch (Exception e) {
            throw new RemoteException("DB error during updateDetails", e);
        }
    }

    @Override
    public int leaveBalance(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        try {
            int year = Year.now().getValue();
            int bal = leaveBalanceRepo.getYearBalanceOrCreate(s.getUserId(), year, 15);
            audit.log("leaveBalance by " + s.getUserId() + " year=" + year + " bal=" + bal);
            return bal;

        } catch (Exception e) {
            throw new RemoteException("DB error during leaveBalance", e);
        }
    }
    
    @Override
    public Employee getMyProfile(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        try {
            Employee basic = employees.findBasic(s.getUserId());
            if (basic == null) throw new IllegalStateException("Employee not found");

            // fetch details row
            EmployeeDetailsRepository.DetailsRow d = details.find(s.getUserId());
            if (d != null) {
                basic.setPhoneNo(d.phone);
                basic.setEmergencyName(d.emergencyName);
                basic.setEmergencyPhoneNo(d.emergencyNo);
                basic.setEmergencyRelationship(d.emergencyRelationship);
            }
            return basic;

        } catch (Exception e) {
            throw new RemoteException("DB error during getMyProfile", e);
        }
    }
    
    @Override
    public int applyLeave(UserSession session, String leaveType, String startDateYYYYMMDD, String endDateYYYYMMDD, String reason)
            throws RemoteException {

        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        try {
            if (leaveType == null || leaveType.isBlank()) throw new IllegalArgumentException("Leave type required");
            LocalDate start = LocalDate.parse(startDateYYYYMMDD.trim());
            LocalDate end = LocalDate.parse(endDateYYYYMMDD.trim());
            if (end.isBefore(start)) throw new IllegalArgumentException("End date must be >= start date");

            int daysRequested = (int) ChronoUnit.DAYS.between(start, end) + 1; // inclusive
            if (daysRequested <= 0) throw new IllegalArgumentException("Invalid day count");

            int year = Year.now().getValue();
            int bal = leaveBalanceRepo.getYearBalanceOrCreate(s.getUserId(), year, 15);

            if (daysRequested > bal) {
                throw new IllegalStateException("Not enough leave balance. Balance=" + bal + ", requested=" + daysRequested);
            }

            int leaveId = leaveRepo.insertApplication(s.getUserId(), leaveType.trim(), start, end, daysRequested, reason);
            audit.log("applyLeave: emp=" + s.getUserId() + " leaveId=" + leaveId + " days=" + daysRequested);
            return leaveId;

        } catch (Exception e) {
            throw new RemoteException("DB error during applyLeave", e);
        }
    }

    @Override
    public String viewMyLeaveApplications(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        try {
            List<LeaveApplicationsRepository.LeaveRow> rows = leaveRepo.listByEmployee(s.getUserId());
            if (rows.isEmpty()) return "No leave applications found.";

            StringBuilder sb = new StringBuilder();
            sb.append("=== MY LEAVE APPLICATIONS ===\n");
            for (var r : rows) {
                sb.append("ID: ").append(r.leaveId)
                  .append(" | ").append(r.leaveType)
                  .append(" | ").append(r.startDate).append(" -> ").append(r.endDate)
                  .append(" | Days: ").append(r.daysRequested)
                  .append(" | Status: ").append(r.status)
                  .append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RemoteException("DB error during viewMyLeaveApplications", e);
        }
    }

    @Override
    public String viewMyLeaveHistory(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        try {
            List<LeaveApplicationsRepository.LeaveRow> rows = leaveRepo.listHistoryByEmployee(s.getUserId());
            if (rows.isEmpty()) return "No leave history found.";

            StringBuilder sb = new StringBuilder();
            sb.append("=== MY LEAVE HISTORY ===\n");
            for (var r : rows) {
                sb.append("ID: ").append(r.leaveId)
                  .append(" | ").append(r.leaveType)
                  .append(" | ").append(r.startDate).append(" -> ").append(r.endDate)
                  .append(" | Days: ").append(r.daysRequested)
                  .append(" | Status: ").append(r.status)
                  .append(" | DecidedBy: ").append(r.decidedBy == null ? "-" : r.decidedBy)
                  .append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RemoteException("DB error during viewMyLeaveHistory", e);
        }
    }

    @Override
    public String viewPendingLeaveApplications(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.HR) throw new SecurityException("HR only");

        try {
            List<LeaveApplicationsRepository.LeaveRow> rows = leaveRepo.listPending();
            if (rows.isEmpty()) return "No pending leave applications.";

            StringBuilder sb = new StringBuilder();
            sb.append("=== PENDING LEAVE APPLICATIONS ===\n");
            for (var r : rows) {
                sb.append("ID: ").append(r.leaveId)
                  .append(" | EMP: ").append(r.employeeId)
                  .append(" | ").append(r.leaveType)
                  .append(" | ").append(r.startDate).append(" -> ").append(r.endDate)
                  .append(" | Days: ").append(r.daysRequested)
                  .append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RemoteException("DB error during viewPendingLeaveApplications", e);
        }
    }

    @Override
    public void decideLeave(UserSession session, int leaveId, boolean approve) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.HR) throw new SecurityException("HR only");

        try {
            var row = leaveRepo.findById(leaveId);
            if (row == null) throw new IllegalArgumentException("Leave ID not found");
            if (!"PENDING".equalsIgnoreCase(row.status)) throw new IllegalStateException("Leave is not pending");

            if (approve) {
                int year = Year.now().getValue();
                int bal = leaveBalanceRepo.getYearBalanceOrCreate(row.employeeId, year, 15);
                if (row.daysRequested > bal) {
                    throw new IllegalStateException("Cannot approve: insufficient balance. Balance=" + bal);
                }
                // deduct
                leaveBalanceRepo.setBalance(row.employeeId, year, bal - row.daysRequested);
                leaveRepo.setDecision(leaveId, "APPROVED", s.getUserId());
                audit.log("decideLeave APPROVED: leaveId=" + leaveId + " by=" + s.getUserId());
            } else {
                leaveRepo.setDecision(leaveId, "REJECTED", s.getUserId());
                audit.log("decideLeave REJECTED: leaveId=" + leaveId + " by=" + s.getUserId());
            }

        } catch (Exception e) {
            throw new RemoteException("DB error during decideLeave", e);
        }
    }
    
}