package dev.kiretori.smartcity.consumers;


import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;

public class Consumer implements Runnable{

    protected String topic;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public Consumer(String topic) {
        this.topic = topic;
    }

    public InfluxDBClient getInfluxDbClient() {
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

    @Override
    public void run() {
        // Set up the consumer configuration
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                  "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                  "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topic));

        // Poll for new messages
        System.out.println("Waiting for messages...");

        InfluxDBClient influxDBClient = getInfluxDbClient();
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        // TODO: Inheritance to add multiple consumers because writing to influxDB is different for each type of sensor
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("Received message: key=%s,\n value=%s,\n partition=%d,\n offset=%d%n",
                        record.key(), record.value(), record.partition(), record.offset());
            }
        }
    }
}
