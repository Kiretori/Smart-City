package dev.kiretori.smartcity.producers;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class WaterProducer extends BaseProducer implements Runnable{

    private final UUID sensorId; 
    private static final Random random = new Random();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public WaterProducer() {
        this.sensorId = UUID.randomUUID();
    }

    public UUID getSensorId() {
        return sensorId;
    }


    @Override
    protected String getTopic() {
        return "test-water";
    }

    @Override
    public void run() {
        int x = 5;
        scheduler.scheduleAtFixedRate(() -> sendData(sensorId), 0, x, TimeUnit.SECONDS); // Send data every x seconds
    }

    @Override
    protected Map<String, Object> simulateSensorData(UUID sensorId) {
        Map<String, Object> sensorData = new HashMap<>();

        sensorData.put("sensorId", sensorId);
        sensorData.put("type", "Water");
        LocalDateTime currentDate = LocalDateTime.now();
        sensorData.put("timestamp", currentDate.format(dtf));

        int hour = currentDate.getHour();
        DayOfWeek day = currentDate.getDayOfWeek();

    // Base multipliers
        double flowMultiplier;
        if (hour >= 6 && hour < 9) {
            flowMultiplier = 1.5; // Morning peak
        } else if (hour >= 17 && hour < 21) {
            flowMultiplier = 1.8; // Evening peak
        } else if (hour >= 0 && hour < 5) {
            flowMultiplier = 0.3; // Night low usage
        } else {
            flowMultiplier = 1.0; // Normal
        }

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            flowMultiplier *= 1.2; // Weekend boost
        }

        double baseFlow = random.nextDouble() * 30; // L/min, baseline
        double flowRate = round(baseFlow * flowMultiplier, 2);
        double totalVolume = round(random.nextDouble() * 10000, 2); // m³

        double waterPressure = round(2.5 + random.nextDouble() * 2, 2); // 2.5–4.5 bars
        double waterTemperature = round(8 + random.nextDouble() * 10, 1); // 8–18°C

        sensorData.put("flowRate", flowRate);
        sensorData.put("totalVolume", totalVolume);
        sensorData.put("waterPressure", waterPressure);
        sensorData.put("waterTemperature", waterTemperature); 
        
        boolean leakDetected = random.nextDouble() < 0.05; // 5% chance of leak
        sensorData.put("leakDetected", leakDetected);
        if (leakDetected) {
            sensorData.put("alarmStatus", "LEAK_DETECTED");
            sensorData.put("alarmSeverity", "HIGH");
        } else {
            sensorData.put("alarmStatus", "NORMAL");
            sensorData.put("alarmSeverity", "NONE");
        }
        
        return sensorData;

    }

    


}
