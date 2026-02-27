package Server;

import Common.Authorization;
import Common.Employee;
import Common.UserRole;
import Common.UserSession;

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
}