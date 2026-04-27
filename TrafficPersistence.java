package Project;

import java.sql.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

/**
 * THE DATA LAYER: TrafficPersistence
 * Concepts: JDBC (Java Database Connectivity)
 * This class handles all communication with the MySQL database and local file auditing.
 * Updated to store more detailed analytics: Road Names and Event Types.
 */
public class TrafficPersistence {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/traffic_db";
    private static final String USER = "root";
    private static final String PASS = "root"; 
    
    // Updated file path for local audit logs
    private static final String LOG_PATH = "C:\\Users\\anany\\OneDrive\\javaproject\\log.txt";

    public TrafficPersistence() {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver missing! Ensure the Connector/J JAR is in your Build Path.");
        }
    }

    /**
     * Maps Road IDs to Human-Readable Names for better Database Analytics.
     */
    private String getRoadName(int roadId) {
        switch (roadId) {
            case 0: return "NORTH_ROAD";
            case 1: return "EAST_ROAD";
            case 2: return "SOUTH_ROAD";
            case 3: return "WEST_ROAD";
            default: return "UNKNOWN_ROAD";
        }
    }

    /**
     * Saves detailed system events to both the database and the local log file.
     * Stores Road Name and Event Type for superior urban data tracking.
     */
    public void saveEvent(int roadId, String state, boolean isEmergency) {
        String roadName = getRoadName(roadId);
        String eventType = isEmergency ? "EMERGENCY_PROTOCOL" : "SYSTEM_CYCLE";

        // 1. Save to Database with expanded fields
        // Ensure your table has: road_name (VARCHAR), road_id (INT), event_type (VARCHAR), state (VARCHAR), is_emergency (BOOLEAN)
        String sql = "INSERT INTO traffic_logs (road_name, road_id, event_type, state, is_emergency, log_time) VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, roadName);
            pstmt.setInt(2, roadId);
            pstmt.setString(3, eventType);
            pstmt.setString(4, state);
            pstmt.setBoolean(5, isEmergency);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("MySQL Persistence Error: " + e.getMessage());
        }

        // 2. Save to Local File (Audit Log) with the same detailed format
        saveToFile(roadName, roadId, eventType, state, isEmergency);
    }

    /**
     * Internal method to handle detailed file writing logic.
     */
    private void saveToFile(String roadName, int roadId, String eventType, String state, boolean isEmergency) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] [%s] %s (ID: %d) | STATE: %s | PRIORITY: %b\n", 
                                        timestamp, eventType, roadName, roadId, state, isEmergency);

        File logFile = new File(LOG_PATH);
        File parentDir = logFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("File Write Failed: " + e.getMessage());
        }
    }
}