package dev.kiretori.smartcity.influx;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

public class InfluxDBService {

    private static InfluxDBClient createInfluxDBClient(String topic) {
        String bucket;
        String org = "docs";
        char[] token = "admintoken".toCharArray();
        switch (topic) {
            case "waste", "test-waste" -> {
                bucket = "waste_data";
            }
            case "water", "test-water" -> {
                bucket = "water_data";
            }
            case "traffic", "test-traffic" -> {
                bucket = "traffic_data";
            }
            case "energy", "test-energy" -> {
                bucket = "energy_data";
            }
            default -> {
                bucket = "water_data";
            }
        }
        return InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);
    }


    public static InfluxDBClient getInfluxClient(String topic) {
        return createInfluxDBClient(topic);
    }
}