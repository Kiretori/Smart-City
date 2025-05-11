package org.yourcompany.smartcity.producers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class WasteBinProducer extends BaseProducer implements Runnable {

    private final UUID sensorId;
    private static final Random random = new Random();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public WasteBinProducer() {
        this.sensorId = UUID.randomUUID();
    }

    public UUID getSensorId() {
        return sensorId;
    }

    @Override
    protected String getTopic() {
        return "test-waste";
    }

    @Override
    public void run() {
        int x = 5;
        scheduler.scheduleAtFixedRate(() -> sendData(sensorId), 0, x, TimeUnit.SECONDS); // Send data every x seconds
    }

    @Override
    protected Map<String, Object> simulateSensorData(UUID sensorId) {
        Map<String, Object> sensorData = new HashMap<>();
        
        // Basic sensor identification
        sensorData.put("sensorId", sensorId);
        sensorData.put("type", "WASTE_BIN");
        sensorData.put("timestamp", LocalDateTime.now().format(dtf));
        
        // Bin specifications
        double maxCapacity = 1000.0; // liters
        double maxWeight = 250.0; // kg
        
        // Sensor readings
        double fillLevel = random.nextDouble() * 100; // percentage
        sensorData.put("fillLevel", round(fillLevel, 1));  // %
        sensorData.put("remainingCapacity", round((100 - fillLevel) / 100 * maxCapacity, 1));  // liters
        sensorData.put("wasteWeight", round(fillLevel / 100 * maxWeight * (0.8 + random.nextDouble() * 0.4), 1));  // kg
        sensorData.put("internalTemperature", round(random.nextDouble() * 30 + 5, 1));  // °C
        sensorData.put("fillRate", round(random.nextDouble() * 5, 2));  // % per day
        
        // Status readings
        boolean binOpen = random.nextDouble() < 0.01; // 1% chance the bin is open
        boolean binTiltDetected = random.nextDouble() < 0.005; // 0.5% chance the bin is tilted
        boolean fireDetected = random.nextDouble() < 0.001; // 0.1% chance of fire
        
        sensorData.put("binOpen", binOpen);
        sensorData.put("tiltDetected", binTiltDetected);
        sensorData.put("fireDetected", fireDetected);

        sensorData.put("odorLevel", round(random.nextDouble() * fillLevel / 20, 1));  // 0-5 scale
        
        // Alarm status
        String alarmStatus = "NORMAL";
        String alarmSeverity = "NONE";
        
        if (fireDetected) {
            alarmStatus = "FIRE_DETECTED";
            alarmSeverity = "CRITICAL";
        } else if (binTiltDetected) {
            alarmStatus = "BIN_TILTED";
            alarmSeverity = "HIGH";
        } else if (fillLevel > 90) {
            alarmStatus = "BIN_FULL";
            alarmSeverity = "HIGH";
        } else if (fillLevel > 75) {
            alarmStatus = "BIN_NEARLY_FULL";
            alarmSeverity = "MEDIUM";
        } else if (binOpen) {
            alarmStatus = "BIN_OPEN";
            alarmSeverity = "LOW";
        }
        
        sensorData.put("alarmStatus", alarmStatus);
        sensorData.put("alarmSeverity", alarmSeverity);
        
        return sensorData;

    }


}
