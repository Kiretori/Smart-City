package dev.kiretori.smartcity.consumers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class WasteConsumer extends BaseConsumer implements Runnable {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ObjectMapper mapper = new ObjectMapper();

    public WasteConsumer(String topic, InfluxDBClient influxDBClient) {
        super(topic);
        this.influxDBClient = influxDBClient;
    }

    @Override
    public void run() {
        consumeData();
    }

    @Override
    protected void sendDataToInflux(ConsumerRecord<String, String> records) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Map<String, Object> wasteData;
        try {
            wasteData = mapper.readValue(records.value(), Map.class);
        } catch (JsonProcessingException ex) {
            System.err.println("Error parsing the json");
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.parse((String) wasteData.get("timestamp"), dtf);
        java.time.Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();

        try {

            Point point = Point.measurement(topic)
                    .addTag("sensor_id", (String) wasteData.get("sensorId"))
                    .addTag("type", (String) wasteData.get("type"))
                    .addField("fillLevel", (Double) wasteData.get("fillLevel"))
                    .addField("remainingCapacity", (Double) wasteData.get("remainingCapacity"))
                    .addField("wasteWeight", (Double) wasteData.get("wasteWeight"))
                    .addField("internalTemperature", (Double) wasteData.get("internalTemperature"))
                    .addField("fillRate", (Double) wasteData.get("fillRate"))
                    .addField("binOpen", (Boolean) wasteData.get("binOpen"))
                    .addField("tiltDetected", (Boolean) wasteData.get("tiltDetected"))
                    .addField("fireDetected", (Boolean) wasteData.get("fireDetected"))
                    .addField("odorLevel", (Double) wasteData.get("odorLevel"))
                    .addField("alarmStatus", (String) wasteData.get("alarmStatus"))
                    .addField("alarmSeverity", (String) wasteData.get("alarmSeverity"))
                    .time(instant, WritePrecision.S);

            writeApi.writePoint(point);
            System.out.println("Waste data sent to influxDB");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format");
        }
    }
}
