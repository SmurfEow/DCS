/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;
import java.io.Serializable;

public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    
    private String phoneNo;
    private String emergencyNo;
    private String email;
    
    private int leaveBalance =15;
    
    public Employee(){}
    
    public Employee(String firstName, String lastName, String icPassport){
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
    }
    
    public String getEmployeeId(){
        return employeeId;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getIcPassport(){
        return icPassport;
    }
    public String getPhoneNo(){
        return phoneNo;
    }
    public String getEmergencyNo(){
        return emergencyNo;
    }
    public String getEmail(){
        return email;
    }
    public int getLeaveBalance(){
        return leaveBalance;
    }
    
    public void setEmployeeId(String employeeId){
        this.employeeId = employeeId;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setIcPassport(String icPassport){
        this.icPassport = icPassport;
    }
    public void setPhoneNo(String phoneNo){
        this.phoneNo = phoneNo;
    }
    public void setEmergencyNo(String emergencyNo){
        this.emergencyNo = emergencyNo;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setLeaveBalance(int leaveBalance){
        this.leaveBalance = leaveBalance;
    }
}
