package dev.kiretori.smartcity.producers;

import java.time.LocalDateTime;
import java.util.concurrent.CyclicBarrier;

public class SimulationTimeManager {
    private LocalDateTime currentSimulatedTime;
    private final int timeIncrementHours;
    private final Object timeLock = new Object();
    private final CyclicBarrier barrier;
    
    public SimulationTimeManager(LocalDateTime startTime, int timeIncrementHours, int totalSensors) {
        this.currentSimulatedTime = startTime;
        this.timeIncrementHours = timeIncrementHours;
        this.barrier = new CyclicBarrier(totalSensors, () -> {
            // This runs when all sensors have finished sending data
            advanceTime();
            System.out.println("All sensors completed cycle. Time advanced to: " + getCurrentTime());
        });
    }
    
    public LocalDateTime getCurrentTime() {
        synchronized (timeLock) {
            return currentSimulatedTime;
        }
    }
    
    private void advanceTime() {
        synchronized (timeLock) {
            currentSimulatedTime = currentSimulatedTime.plusHours(timeIncrementHours);
        }
    }
    
    public void waitForAllSensors() throws Exception {
        barrier.await(); // Wait for all sensors to reach this point
    }
    
    public void setTime(LocalDateTime newTime) {
        synchronized (timeLock) {
            this.currentSimulatedTime = newTime;
        }
    }
}