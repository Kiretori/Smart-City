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

public class TrafficConsumer extends BaseConsumer implements Runnable {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ObjectMapper mapper = new ObjectMapper();

    public TrafficConsumer(String topic, InfluxDBClient influxDBClient) {
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
        Map<String, Object> trafficData;
        try {
            trafficData = mapper.readValue(records.value(), Map.class);
        } catch (JsonProcessingException ex) {
            System.err.println("Error parsing the json");
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.parse((String) trafficData.get("timestamp"), dtf);
        java.time.Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();

        try {

            Point point = Point.measurement(topic)
                    .addTag("sensor_id", (String) trafficData.get("sensorId"))
                    .addTag("type", (String) trafficData.get("type"))
                    .addField("vehiclesPerMinute", (int) trafficData.get("vehiclesPerMinute"))
                    .addField("averageSpeed", (Double) trafficData.get("averageSpeed"))
                    .addField("queueLength", (int) trafficData.get("queueLength"))
                    .addField("queueLengthMeters", (Double) trafficData.get("queueLengthMeters"))
                    .addField("incidentDetected", (Boolean) trafficData.get("incidentDetected"))
                    .addField("alarmStatus", (String) trafficData.get("alarmStatus"))
                    .addField("alarmSeverity", (String) trafficData.get("alarmSeverity"))
                    .time(instant, WritePrecision.S);

            writeApi.writePoint(point);
            System.out.println("Traffic data sent to influxDB");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format");
        }
    }
}
