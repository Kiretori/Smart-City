package dev.kiretori.smartcity.producers;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseProducer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Subclasses must define how to simulate sensor data
    protected abstract Map<String, Object> simulateSensorData(UUID sensorId);

    // Subclasses must define the topic
    protected abstract String getTopic();

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public void sendData(UUID sensorId) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            String key = "sensor-" + sensorId;
            String value = objectMapper.writeValueAsString(simulateSensorData(sensorId));
            ProducerRecord<String, String> record = new ProducerRecord<>(getTopic(), key, value);

            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception == null) {
                    System.out.printf("Sent message to %s partition %d offset %d%n",
                            metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}