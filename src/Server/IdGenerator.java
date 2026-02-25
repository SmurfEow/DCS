/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger employeeCounter = new AtomicInteger(1);
    private static final AtomicInteger humanResourceCounter = new AtomicInteger(1);
    
    public static String nextEmployeeCounter(){
        return String.format("E-%06d", employeeCounter.getAndIncrement());
    }
    public static String nextHumanResourceCounter(){
        return String.format("H-%06d", humanResourceCounter.getAndIncrement());
    }
}
