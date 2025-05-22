package dev.kiretori.smartcity.producers;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EnergyProducer extends BaseProducer implements Runnable {

    private final UUID sensorId;
    private static final Random random = new Random();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SimulationTimeManager timeManager;


    public EnergyProducer(SimulationTimeManager timeManager) {
        this.sensorId = UUID.randomUUID();
        this.timeManager = timeManager;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    @Override
    protected String getTopic() {
        return "energy";
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
        sensorData.put("type", "SMART_ENERGY_METER");
        sensorData.put("timestamp", timeManager.getCurrentTime().format(dtf));

        // Sensor readings
        double instantPower = random.nextDouble() * 10; // kW
        sensorData.put("instantPower", round(instantPower, 3));  // kW
        sensorData.put("cumulativeEnergy", round(random.nextDouble() * 50000, 2));  // kWh
        sensorData.put("voltage", round(random.nextDouble() * 10 + 230, 1));  // V
        sensorData.put("current", round(instantPower * 1000 / 230, 2));  // A
        sensorData.put("powerFactor", round(random.nextDouble() * 0.2 + 0.8, 2));  // 0.8-1.0
        sensorData.put("frequency", round(random.nextDouble() * 0.2 + 49.9, 2));  // Hz

        return sensorData;

    }


}
