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


public class EnergyProducer extends BaseProducer implements Runnable {

    private final UUID sensorId;
    private static final Random random = new Random();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public EnergyProducer() {
        this.sensorId = UUID.randomUUID();
    }

    public UUID getSensorId() {
        return sensorId;
    }

    @Override
    protected String getTopic() {
        return "test-energy";
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
        sensorData.put("type", "SMART_ENERGY_METER");
        sensorData.put("timestamp", LocalDateTime.now().format(dtf));

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
