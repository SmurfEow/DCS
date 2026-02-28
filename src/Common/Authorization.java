package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Authorization extends Remote {

    String ping() throws RemoteException;

    UserSession login(String userId, String password) throws RemoteException;
    void logout(UserSession session) throws RemoteException;

    // HR
    Employee registerEmployee(UserSession session, String firstName, String lastName, String icPassport, String initPass)
            throws RemoteException;

    // Staff
    Employee updateDetails(UserSession session, Employee updated) throws RemoteException;
    int leaveBalance(UserSession session) throws RemoteException;
    Employee getMyProfile(UserSession session) throws RemoteException;

    // =========================
    // LEAVE (STAFF)
    // =========================
    int applyLeave(UserSession session,
                   String leaveType,
                   String startDateYYYYMMDD,
                   String endDateYYYYMMDD,
                   String reason) throws RemoteException;

    String viewMyLeaveApplications(UserSession session) throws RemoteException;  // includes pending
    String viewMyLeaveHistory(UserSession session) throws RemoteException;       // approved/rejected only

    // =========================
    // LEAVE (HR)
    // =========================
    String viewPendingLeaveApplications(UserSession session) throws RemoteException;

    void decideLeave(UserSession session,
                     int leaveId,
                     boolean approve) throws RemoteException;
}