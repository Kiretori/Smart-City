package dev.kiretori.smartcity.producers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TrafficProducer extends BaseProducer implements Runnable {

    private final UUID sensorId;
    private static final Random random = new Random();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SimulationTimeManager timeManager;


    public TrafficProducer(SimulationTimeManager timeManager) {
        this.sensorId = UUID.randomUUID();
        this.timeManager = timeManager;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    @Override
    protected String getTopic() {
        return "traffic";
    }

    @Override
    public void run() {
        int dataSendInterval = 1; // seconds between data sends
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Send data
                sendData(sensorId);
                
                // Wait for all other sensors to finish sending their data
                // Time will advance automatically when all sensors reach this point
                timeManager.waitForAllSensors();
                
            } catch (Exception e) {
                System.err.println("Error in sensor " + sensorId + ": " + e.getMessage());
            }
        }, 0, dataSendInterval, TimeUnit.SECONDS);
    }

    @Override
    protected Map<String, Object> simulateSensorData(UUID sensorId) {
        Map<String, Object> sensorData = new HashMap<>();
        
        // Basic sensor identification
        sensorData.put("sensorId", sensorId);
        sensorData.put("type", "TRAFFIC");
        sensorData.put("timestamp", timeManager.getCurrentTime().format(dtf));
        
        // Time-based traffic patterns
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        boolean isRushHour = (hour >= 7 && hour <= 9) || (hour >= 16 && hour <= 19);
        boolean isWeekend = now.getDayOfWeek().getValue() >= 6;
        
        // Traffic flow base rates adjusted for time of day
        double trafficMultiplier = 0.5; // base rate
        if (isRushHour && !isWeekend) {
            trafficMultiplier = 2.0;
        } else if (!isWeekend && hour >= 10 && hour <= 15) {
            trafficMultiplier = 1.0;
        } else if (isWeekend && hour >= 10 && hour <= 20) {
            trafficMultiplier = 1.2;
        }
        
        // Calculate vehicles per minute based on multiplier with some randomness
        int vehiclesPerMinute = (int) (trafficMultiplier * (10 + random.nextInt(20)));
        
        // Sensor readings
        sensorData.put("vehiclesPerMinute", vehiclesPerMinute);
        sensorData.put("averageSpeed", round(random.nextDouble() * 30 + 20, 1));  // km/h

        double laneOccupancy = trafficMultiplier * random.nextDouble() * 50;

        int queueLength;

        if (laneOccupancy > 70) {
            queueLength = random.nextInt(30) + 20; // 20-50 vehicles in queue during high occupancy
        } else if (laneOccupancy > 40) {
            queueLength = random.nextInt(10) + 5;  // 5-15 vehicles in queue during medium occupancy
        } else {
            queueLength = random.nextInt(5);       // 0-4 vehicles in queue during low occupancy
        }
        sensorData.put("queueLength", queueLength);
        sensorData.put("queueLengthMeters", queueLength * (5 + random.nextDouble() * 2));  // average vehicle length + spacing
        
        // Incident detection
        boolean incidentDetected = random.nextDouble() < 0.02; // 2% chance of incident
        sensorData.put("incidentDetected", incidentDetected);
        

        String alarmStatus = "NONE";
        String alarmSeverity = "NONE";
        if (incidentDetected) {
            alarmStatus = "INCIDENT_DETECTED";
            alarmSeverity = "HIGH";
        } else if (laneOccupancy > 80) {
            alarmStatus = "SEVERE_CONGESTION";
            alarmSeverity = "MEDIUM";
        } else if (laneOccupancy > 60) {
            alarmStatus = "MODERATE_CONGESTION";
            alarmSeverity = "LOW";
        }
        
        sensorData.put("alarmStatus", alarmStatus);
        sensorData.put("alarmSeverity", alarmSeverity);
        
        return sensorData;

    }
}
