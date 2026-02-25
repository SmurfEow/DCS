/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Common.Authorization;
import Common.Employee;
import Common.UserRole;
import Common.UserSession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServiceImpl extends UnicastRemoteObject implements Authorization {

    private final Session sessionManager = new Session();
    private final ConcurrentSession credentialStore = new ConcurrentSession();
    private final EmployeeSession employeeStore = new EmployeeSession();
    private final ActivityLog audit = new ActivityLog("audit.log");

    protected ServiceImpl() throws RemoteException {
        super();

        // Default HR account
        String hrId = IdGenerator.nextHumanResourceCounter();
        String salt = PasswordHash.newSalt();
        String hash = PasswordHash.hash(salt, "hr123");

        credentialStore.put(hrId, new ConcurrentSession.cred(salt, hash, UserRole.HR));

        System.out.println("Default HR created: " + hrId + " pw=hr123");
        audit.log("Server started. Default HR=" + hrId);
    }

    @Override
    public String ping() throws RemoteException {
        audit.log("ping()");
        return "Server is valid!";
    }

    @Override
    public UserSession login(String userId, String password) throws RemoteException {
        ConcurrentSession.cred c = credentialStore.get(userId);
        if (c == null) {
            audit.log("login fail no user: " + userId);
            throw new SecurityException("Invalid credentials");
        }

        if (!PasswordHash.verification(c.salt, c.hash, password)) {
            audit.log("login fail bad pw: " + userId);
            throw new SecurityException("Invalid credentials");
        }

        UserSession s = sessionManager.create(c.role, userId);
        audit.log("login ok: " + userId + " role=" + c.role);
        return s;
    }

    @Override
    public void logout(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        sessionManager.remove(session);
        audit.log("logout: " + s.getUserId());
    }

    @Override
    public Employee registerEmployee(UserSession session, String firstName, String lastName, String icPassport, String initPass)
            throws RemoteException {

        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.HR) throw new SecurityException("HR only");

        String empId = IdGenerator.nextEmployeeCounter();

        Employee e = new Employee(firstName, lastName, empId);
        // Your Employee constructor might be different — if so, set fields with setters:
        // e.setEmployeeId(empId); e.setFirstName(firstName); ...

        employeeStore.put(e);

        String salt = PasswordHash.newSalt();
        String hash = PasswordHash.hash(salt, initPass);
        credentialStore.put(empId, new ConcurrentSession.cred(salt, hash, UserRole.STAFF));

        audit.log("HR " + s.getUserId() + " registered " + empId);
        return e;
    }

    @Override
    public Employee updateDetails(UserSession session, Employee updated) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        if (updated == null) throw new IllegalArgumentException("Missing employee data");

        // Staff can only update themselves
        if (!s.getUserId().equals(updated.getEmployeeId())) {
            throw new SecurityException("Cannot update other users");
        }

        Employee existing = employeeStore.get(s.getUserId());
        if (existing == null) throw new IllegalStateException("Employee not found");

        // Update safe fields (depends on your Employee fields)
        existing.setPhoneNo(updated.getPhoneNo());
        existing.setEmail(updated.getEmail());
        existing.setEmergencyNo(updated.getEmergencyNo());

        audit.log("Staff " + s.getUserId() + " updated details");
        return existing;
    }

    @Override
    public int leaveBalance(UserSession session) throws RemoteException {
        UserSession s = sessionManager.require(session);
        if (s.getRole() != UserRole.STAFF) throw new SecurityException("Staff only");

        Employee e = employeeStore.get(s.getUserId());
        if (e == null) throw new IllegalStateException("Employee not found");

        audit.log("leaveBalance by " + s.getUserId());
        return e.getLeaveBalance();
    }
}
