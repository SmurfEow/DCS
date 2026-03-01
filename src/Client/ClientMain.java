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
            System.out.println("1) Register Employee");
            System.out.println("2) View Pending Leave Requests");
            System.out.println("3) Approve / Reject Leave");
            System.out.println("4) Generate Yearly Leave Report");
            System.out.println("5) Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {

                    case "1": {
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
                    }

                    case "2": {
                        String pending = service.viewPendingLeaveApplications(session);
                        System.out.println(pending);
                        break;
                    }

                    case "3": {
                        // Show pending first (nice UX)
                        String pending = service.viewPendingLeaveApplications(session);
                        System.out.println(pending);

                        System.out.print("\nEnter Leave ID to decide (or 0 to cancel): ");
                        int leaveId;
                        try {
                            leaveId = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException nfe) {
                            System.out.println(" Invalid leave ID.");
                            break;
                        }

                        if (leaveId == 0) {
                            System.out.println("Cancelled.");
                            break;
                        }

                        System.out.print("Approve? (Y/N): ");
                        String decision = sc.nextLine().trim();

                        boolean approve;
                        if (decision.equalsIgnoreCase("Y")) {
                            approve = true;
                        } else if (decision.equalsIgnoreCase("N")) {
                            approve = false;
                        } else {
                            System.out.println(" Invalid choice. Enter Y or N.");
                            break;
                        }

                        service.decideLeave(session, leaveId, approve);
                        System.out.println(approve ? " Leave approved." : " Leave rejected.");
                        break;
                    }
                    case "4": {
                        System.out.print("Enter Employee ID: ");
                        String empId = sc.nextLine().trim();

                        System.out.print("Enter Year: ");
                        int year;
                        try {
                            year = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException nfe) {
                            System.out.println(" Invalid year.");
                            break;
                        }

                        String report = service.generateYearlyLeaveReport(session, empId, year);
                        System.out.println(report);
                        break;
                    }

                    case "5": {
                        safeLogout(service, session);
                        System.out.println("Logged out.");
                        return null;
                    }

                    default:
                        System.out.println("Invalid option.");
                }

            } catch (Exception ex) {
                if (isSessionExpired(ex)) {
                    System.out.println(" Session expired. Please login again.");
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
            System.out.println("3) Apply Leave");
            System.out.println("4) View Leave Application");
            System.out.println("5) View Leave History");
            System.out.println("6) Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {

                    case "1": {
                        Employee profile = service.getMyProfile(session);

                        while (true) {
                            System.out.println("\n=== UPDATE DETAILS ===");
                            System.out.println("1) Phone                   : " + nullSafe(profile.getPhoneNo()));
                            System.out.println("2) Emergency Family Name   : " + nullSafe(profile.getEmergencyName()));
                            System.out.println("3) Emergency Contact No    : " + nullSafe(profile.getEmergencyNo()));
                            System.out.println("4) Emergency Relationship  : " + nullSafe(profile.getEmergencyRelationship()));
                            System.out.println("5) Save & Exit");
                            System.out.print("Select a field to edit: ");

                            String pick = sc.nextLine().trim();
                            if (pick.equals("5")) break;

                            switch (pick) {
                                case "1":
                                    System.out.print("New Phone: ");
                                    profile.setPhoneNo(sc.nextLine().trim());
                                    break;
                                case "2":
                                    System.out.print("New Emergency Family Name: ");
                                    profile.setEmergencyName(sc.nextLine().trim());
                                    break;
                                case "3":
                                    System.out.print("New Emergency Contact No: ");
                                    profile.setEmergencyPhoneNo(sc.nextLine().trim());
                                    break;
                                case "4":
                                    System.out.print("New Emergency Relationship: ");
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
                        updated.setEmergencyPhoneNo(profile.getEmergencyNo()); // OK (your getter name)
                        updated.setEmergencyRelationship(profile.getEmergencyRelationship());

                        service.updateDetails(session, updated);
                        System.out.println("Details updated successfully!");
                        break;
                    }

                    case "2": {
                        int bal = service.leaveBalance(session);
                        System.out.println("Leave balance: " + bal + " day(s)");
                        break;
                    }

                    case "3": {
                        // Apply Leave flow (loops until user cancels or submits once)
                        while (true) {
                            System.out.println("\nSelect Leave Type:");
                            System.out.println("1) Annual Leave");
                            System.out.println("2) Medical Leave");
                            System.out.println("3) Emergency Leave");
                            System.out.println("4) Unpaid Leave");
                            System.out.println("5) Cancel / Back");
                            System.out.print("Choice: ");

                            String typeChoice = sc.nextLine().trim();
                            String type;

                            switch (typeChoice) {
                                case "1": type = "ANNUAL"; break;
                                case "2": type = "MEDICAL"; break;
                                case "3": type = "EMERGENCY"; break;
                                case "4": type = "UNPAID"; break;
                                case "5":
                                    System.out.println("Cancelled.");
                                    return session; // back to staff menu
                                default:
                                    System.out.println(" Invalid option.");
                                    continue; 
                            }

                            // Date input
                            System.out.print("Start Date (YYYY-MM-DD): ");
                            String start = sc.nextLine().trim();

                            System.out.print("End Date (YYYY-MM-DD): ");
                            String end = sc.nextLine().trim();

                            System.out.print("Reason: ");
                            String reason = sc.nextLine().trim();

                            // Client-side validation
                            try {
                                java.time.LocalDate sDate = java.time.LocalDate.parse(start);
                                java.time.LocalDate eDate = java.time.LocalDate.parse(end);

                                if (eDate.isBefore(sDate)) {
                                    System.out.println(" End date must be after or same as start date.");
                                    continue;
                                }

                                if (sDate.isBefore(java.time.LocalDate.now())) {
                                    System.out.println(" Start date cannot be in the past.");
                                    continue;
                                }

                                long days = java.time.temporal.ChronoUnit.DAYS.between(sDate, eDate) + 1;

                                System.out.println("\n=== CONFIRM LEAVE ===");
                                System.out.println("Type  : " + type);
                                System.out.println("From  : " + sDate);
                                System.out.println("To    : " + eDate);
                                System.out.println("Days  : " + days);
                                System.out.println("Reason: " + reason);
                                System.out.print("Confirm submit? (Y/N): ");

                                String confirm = sc.nextLine().trim();
                                if (!confirm.equalsIgnoreCase("Y")) {
                                    System.out.println("Submission cancelled.");
                                    return session; // back to staff menu
                                }

                                int leaveId = service.applyLeave(session, type, start, end, reason);
                                System.out.println(" Leave submitted. ID: " + leaveId + " (Status: PENDING)");
                                return session; // submit once then back to staff menu

                            } catch (Exception ex) {
                                System.out.println(" Invalid date format. Use YYYY-MM-DD (example: 2026-03-10).");
                            }
                        }
                    }

                    case "4": {
                        String apps = service.viewMyLeaveApplications(session);
                        System.out.println(apps);
                        break;
                    }

                    case "5": {
                        String history = service.viewMyLeaveHistory(session);
                        System.out.println(history);
                        break;
                    }

                    case "6": {
                        safeLogout(service, session);
                        System.out.println("Logged out.");
                        return null;
                    }

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
    

