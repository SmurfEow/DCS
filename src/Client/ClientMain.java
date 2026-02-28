package Client;

import Common.Authorization;
import Common.Employee;
import Common.UserRole;
import Common.UserSession;

import java.rmi.Naming;
import java.util.Scanner;

public class ClientMain {

    private static final String SERVER_IP = "192.168.1.19";
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
                        Employee profile = service.getMyProfile(session);
                        while(true){
                            System.out.println("\n=== UPDATE DETAILS ===");
                            System.out.println("1. Phone                        :" + nullSafe(profile.getPhoneNo()));
                            System.out.println("2. Emergency Family name        :" + nullSafe(profile.getPhoneNo()));
                            System.out.println("3. Emergency Contact            :" + nullSafe(profile.getPhoneNo()));
                            System.out.println("4. Emergenc Relationship        :" + nullSafe(profile.getPhoneNo()));
                            System.out.println("5. Save & Exit                  :");
                            System.out.println("Select a field to edit:         :");
                            String pick = sc.nextLine().trim();
                            
                            if (pick.equals("5")) break;
                            
                            switch(pick){
                                case "1":
                                    System.out.println("New Phone: ");
                                    profile.setPhoneNo(sc.nextLine().trim());
                                    break;
                                case "2":
                                    System.out.println("New Emergency Family: ");
                                    profile.setEmergencyName(sc.nextLine().trim());
                                    break;
                                case "3":
                                    System.out.println("New Emergency No: ");
                                    profile.setEmergencyPhoneNo(sc.nextLine().trim());
                                    break;
                                case "4":
                                    System.out.println("New Emergency Relationship: ");
                                    profile.setEmergencyRelationship(sc.nextLine().trim());
                                    break;
                                default:
                                    System.out.println("Invalid option");
                            }
                        }
                        Employee updated = new Employee();
                        updated.setEmployeeId(session.getUserId());
                        updated.setPhoneNo(profile.getPhoneNo());
                        updated.setEmergencyName(profile.getEmergencyName());
                        updated.setEmergencyPhoneNo(profile.getEmergencyNo());
                        updated.setEmergencyRelationship(profile.getEmergencyRelationship());
                        
                        service.updateDetails(session, updated);
                        System.out.println("Details updated successfully!");
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
    private static String nullSafe(String s){
        return s == null || s.isBlank() ? "-" : s;
    }
}
    

