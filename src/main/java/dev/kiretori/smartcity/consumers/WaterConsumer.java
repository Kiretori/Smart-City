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

public class WaterConsumer extends BaseConsumer implements Runnable {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ObjectMapper mapper = new ObjectMapper();

    public WaterConsumer(String topic, InfluxDBClient influxDBClient) {
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
        Map<String, Object> waterData;
        System.out.println(records.value());
        try {
            waterData = mapper.readValue(records.value(), Map.class);
        } catch (JsonProcessingException ex) {
            System.err.println("Error parsing the json");
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.parse((String) waterData.get("timestamp"), dtf);
        java.time.Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();

        try {

            Point point = Point.measurement(topic)
                    .addTag("sensor_id", (String) waterData.get("sensorId"))
                    .addTag("type", (String) waterData.get("type"))
                    .addField("flowRate", (Double) waterData.get("flowRate"))
                    .addField("totalVolume", (Double) waterData.get("totalVolume"))
                    .addField("leakDetected", (boolean) waterData.get("leakDetected"))
                    .addField("alarmStatus", (String) waterData.get("alarmStatus"))
                    .addField("alarmSeverity", (String) waterData.get("alarmSeverity"))
                    .time(instant, WritePrecision.S);

            writeApi.writePoint(point);
            System.out.println("Data sent to influxDB");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format");
        }
    }
}
