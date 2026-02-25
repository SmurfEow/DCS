/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityLog {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final String filePath;

    public ActivityLog(String filePath) {
        this.filePath = filePath;
    }

    public void log(String msg) {
        exec.submit(() -> {
            try (FileWriter fw = new FileWriter(filePath, true)) {
                fw.write(LocalDateTime.now() + " | " + msg + System.lineSeparator());
            } catch (Exception ignored) {}
        });
    }
    
}
