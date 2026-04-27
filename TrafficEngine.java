package Project;

import java.util.*;

/**
 * THE BRAIN: TrafficEngine
 * Concepts: Multithreading, Synchronization, PriorityQueue, and ITC.
 * This class handles the specific "Serve & Resume" logic where:
 * 1. Current road is interrupted and paused.
 * 2. Emergency road is served for exactly 5 seconds.
 * 3. Only ambulances are allowed to move during the override.
 * 4. The previous road resumes its remaining time.
 */
public class TrafficEngine {
    public int activeRoad = 0;
    public String signalState = "RED";
    public int timer = 0;
    
    // Flag to signal the UI that only ambulances should move
    public boolean emergencyOverride = false; 
    
    // Concept: PriorityQueue for handling multiple emergency requests
    public final PriorityQueue<Integer> emergencyQueue = new PriorityQueue<>();
    
    // Concept: Synchronized ArrayList for thread-safe vehicle management
    public List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<>());
    
    private TrafficPersistence db = new TrafficPersistence();
    private Runnable uiRefresh;
    
    // State variables for "Serve & Resume"
    private Integer suspendedRoad = null; 
    private int suspendedTime = 0;

    /**
     * Links the UI refresh logic (repaint) to the Engine's updates.
     */
    public void setOnUpdate(Runnable r) { 
        this.uiRefresh = r; 
    }

    /**
     * Concept: Inter-Thread Communication (ITC)
     * Wakes up the logic thread immediately when an emergency is added.
     */
    public synchronized void addEmergency(int roadId) {
        if (!emergencyQueue.contains(roadId)) {
            emergencyQueue.add(roadId);
            db.saveEvent(roadId, "AMBULANCE_REQ", true);
            this.notify(); // ITC: Wake up the logic thread from its wait state
        }
    }

    /**
     * Logic Thread: The core loop managing signal transitions.
     */
    public void startLogicThread() {
        new Thread(() -> {
            int rotation = 0;
            while (true) {
                int roadToRun;
                boolean isEm = false;
                int duration = 10;

                synchronized(this) {
                    if (!emergencyQueue.isEmpty()) {
                        // Priority 1: Serve Emergency for 5 seconds
                        roadToRun = emergencyQueue.poll();
                        isEm = true;
                        duration = 5; 
                    } else if (suspendedRoad != null) {
                        // Priority 2: Resume the road that was cut short
                        roadToRun = suspendedRoad;
                        duration = suspendedTime;
                        suspendedRoad = null;
                        suspendedTime = 0;
                        db.saveEvent(roadToRun, "RESUMING_FLOW", false);
                    } else {
                        // Priority 3: Normal Rotation
                        roadToRun = rotation;
                        duration = 10;
                    }
                }

                // Execute the cycle. returns true if finished fully, false if interrupted.
                boolean fullyFinished = runCycle(roadToRun, isEm, duration);

                // Advance the rotation only if a normal cycle finished without interruption
                if (fullyFinished && !isEm) {
                    rotation = (rotation + 1) % 4;
                }
            }
        }).start();
    }

    /**
     * Manages the Green -> Orange -> Red cycle.
     * @return true if completed fully, false if interrupted by a new ambulance.
     */
    private boolean runCycle(int road, boolean isEm, int duration) {
        activeRoad = road;
        emergencyOverride = isEm; // Mode where only ambulances are allowed to move
        
        // --- GREEN PHASE ---
        signalState = "GREEN";
        db.saveEvent(road, isEm ? "EMERGENCY_GREEN" : "GREEN", isEm);
        
        for (int i = duration; i > 0; i--) {
            timer = i; 
            if (uiRefresh != null) uiRefresh.run();
            try { Thread.sleep(1000); } catch (Exception e) {}
            
            // Check for Preemption: If an ambulance comes while a normal road is green
            synchronized(this) {
                if (!isEm && !emergencyQueue.isEmpty()) {
                    suspendedRoad = road; 
                    suspendedTime = i; // Save remaining time to resume later
                    db.saveEvent(road, "PAUSED_FOR_PRIORITY", false);
                    break; // Exit Green phase to start the transition
                }
            }
        }

        // --- ORANGE PHASE (Safety transition) ---
        signalState = "ORANGE";
        db.saveEvent(road, "ORANGE", isEm);
        timer = 3; 
        if (uiRefresh != null) uiRefresh.run();
        try { Thread.sleep(3000); } catch (Exception e) {}

        // --- RED PHASE ---
        signalState = "RED";
        emergencyOverride = false; // Reset movement mode
        db.saveEvent(road, "RED", isEm);
        timer = 0;
        if (uiRefresh != null) uiRefresh.run();
        
        return (suspendedRoad == null || suspendedRoad != road);
    }
}