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

public class EnergyConsumer extends BaseConsumer implements Runnable {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ObjectMapper mapper = new ObjectMapper();

    public EnergyConsumer(String topic, InfluxDBClient influxDBClient) {
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
        Map<String, Object> energyData;
        try {
            energyData = mapper.readValue(records.value(), Map.class);
        } catch (JsonProcessingException ex) {
            System.err.println("Error parsing the json");
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.parse((String) energyData.get("timestamp"), dtf);
        java.time.Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();

        try {

            Point point = Point.measurement(topic)
                    .addTag("sensor_id", (String) energyData.get("sensorId"))
                    .addTag("type", (String) energyData.get("type"))
                    .addField("instantPower", (Double) energyData.get("instantPower"))
                    .addField("cumulativeEnergy", (Double) energyData.get("cumulativeEnergy"))
                    .addField("voltage", (Double) energyData.get("voltage"))
                    .addField("current", (Double) energyData.get("current"))
                    .addField("powerFactor", (Double) energyData.get("powerFactor"))
                    .addField("frequency", (Double) energyData.get("frequency"))
                    .time(instant, WritePrecision.S);

            writeApi.writePoint(point);
            System.out.println("Energy data sent to influxDB");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format");
        }
    }
}
