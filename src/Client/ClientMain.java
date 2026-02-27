package Client;

import Common.Authorization;
import Common.Employee;
import Common.UserRole;
import Common.UserSession;

import java.rmi.Naming;
import java.util.Scanner;

public class ClientMain {

    private static final String SERVER_IP = "192.168.1.5";
    private static final String URL = "rmi://" + SERVER_IP + ":1099/Authorization";

    public static void main(String[] args) {
        try {
            Authorization service = (Authorization) Naming.lookup(URL);
            System.out.println("Connected: " + service.ping());

            Scanner sc = new Scanner(System.in);
            UserSession session = null;

            while (true) {
                while (session == null) {
                    session = doLogin(sc, service);
                }

                if (session.getRole() == UserRole.HR) {
                    session = hrMenu(sc, service, session);
                } else if (session.getRole() == UserRole.STAFF) {
                    session = staffMenu(sc, service, session);
                } else {
                    System.out.println("Unknown role: " + session.getRole());
                    session = null;
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to connect or run client: " + e.getMessage());
        }
    }

    private static UserSession doLogin(Scanner sc, Authorization service) {
        try {
            System.out.println("\n==== LOGIN ====");
            System.out.print("User ID: ");
            String id = sc.nextLine().trim();

            System.out.print("Password: ");
            String pw = sc.nextLine();

            UserSession session = service.login(id, pw);
            System.out.println("Login success. Role: " + session.getRole());
            return session;

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private static UserSession hrMenu(Scanner sc, Authorization service, UserSession session) {
        while (true) {
            System.out.println("\n==== HR MENU ====");
            System.out.println("1) Register Employee");
            System.out.println("2) Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        System.out.print("First Name: ");
                        String fn = sc.nextLine().trim();

                        System.out.print("Last Name: ");
                        String ln = sc.nextLine().trim();

                        System.out.print("IC/Passport: ");
                        String ic = sc.nextLine().trim();

                        System.out.print("Initial Password: ");
                        String initPass = sc.nextLine();

                        Employee e = service.registerEmployee(session, fn, ln, ic, initPass);
                        System.out.println("✅ Employee created: " + e.getEmployeeId());
                        break;

                    case "2":
                        safeLogout(service, session);
                        System.out.println("Logged out.");
                        return null;

                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                if (isSessionExpired(ex)) {
                    System.out.println("⚠ Session expired. Please login again.");
                    return null;
                }
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static UserSession staffMenu(Scanner sc, Authorization service, UserSession session) {
        while (true) {
            System.out.println("\n==== STAFF MENU ====");
            System.out.println("1) Update Details");
            System.out.println("2) View Leave Balance");
            System.out.println("3) Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        // Build updated details object for server to apply
                        Employee updated = new Employee();
                        updated.setEmployeeId(session.getUserId());

                        System.out.print("Phone (Employee): ");
                        updated.setPhoneNo(sc.nextLine().trim());

                        System.out.print("Emergency Name: ");
                        updated.setEmergencyName(sc.nextLine().trim());

                        System.out.print("Emergency Contact No: ");
                        updated.setEmergencyPhoneNo(sc.nextLine().trim()); // ✅ correct setter

                        System.out.print("Emergency Relationship: ");
                        updated.setEmergencyRelationship(sc.nextLine().trim());

                        service.updateDetails(session, updated);
                        System.out.println("✅ Details updated.");
                        break;

                    case "2":
                        int bal = service.leaveBalance(session);
                        System.out.println("Leave balance: " + bal + " day(s)");
                        break;

                    case "3":
                        safeLogout(service, session);
                        System.out.println("Logged out.");
                        return null;

                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                if (isSessionExpired(ex)) {
                    System.out.println("⚠ Session expired. Please login again.");
                    return null;
                }
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void safeLogout(Authorization service, UserSession session) {
        try {
            service.logout(session);
        } catch (Exception ignored) {}
    }

    private static boolean isSessionExpired(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) return false;
        msg = msg.toLowerCase();
        return msg.contains("expired") || msg.contains("invalid");
    }
}
    

