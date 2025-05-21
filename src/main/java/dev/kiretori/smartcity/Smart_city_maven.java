/**
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dev.kiretori.smartcity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.influxdb.client.InfluxDBClient;

import dev.kiretori.smartcity.consumers.EnergyConsumer;
import dev.kiretori.smartcity.consumers.TrafficConsumer;
import dev.kiretori.smartcity.consumers.WasteConsumer;
import dev.kiretori.smartcity.consumers.WaterConsumer;
import dev.kiretori.smartcity.influx.InfluxDBService;
import dev.kiretori.smartcity.producers.EnergyProducer;
import dev.kiretori.smartcity.producers.TrafficProducer;
import dev.kiretori.smartcity.producers.WasteBinProducer;
import dev.kiretori.smartcity.producers.WaterProducer;

/**
 *
 * @author wassim
 */
public class Smart_city_maven {
    public static void main(String[] args) {
        String mode = "none";
        int threadNum = 1;
        if (args.length > 0) {
            mode = args[0];
        }
        switch (mode) {
            case "server" -> {
                System.out.println("Running as server");
                if (args.length > 1) {
                    try {
                        threadNum = Integer.parseInt(args[1]);
                        System.out.println("Thread nums " + threadNum);
                    }
                    catch (NumberFormatException e) {
                    }
                }
                
                // Using virtual threads executor
                try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    List<Future<?>> futures = new ArrayList<>();
                    
                    // Water sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WaterProducer()));
                    }
                    
                    // Energy sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new EnergyProducer()));
                    }
                    
                    // Waste bin sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WasteBinProducer()));
                    }
                    
                    // Traffic sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new TrafficProducer()));
                    }
                    
                    // Wait for all tasks to complete
                    for (Future<?> future : futures) {
                        try {
                            future.get();
                        } catch (Exception e) {
                            System.err.println("Error in virtual thread execution: " + e.getMessage());
                        }
                    }
                }
            }
            case "client" -> {
                System.out.println("Running as client");
                if (args.length > 1) {
                    try {
                        threadNum = Integer.parseInt(args[1]);
                        System.out.println("Thread nums " + threadNum);
                    }
                    catch (NumberFormatException e) {
                    }
                }

                InfluxDBClient waterInfluxDBClient = InfluxDBService.getInfluxClient("test-water");
                InfluxDBClient wasteInfluxDBClient = InfluxDBService.getInfluxClient("test-waste");
                InfluxDBClient trafficInfluxDBClient = InfluxDBService.getInfluxClient("test-traffic");
                InfluxDBClient energyInfluxDBClient = InfluxDBService.getInfluxClient("test-energy");


                try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    List<Future<?>> futures = new ArrayList<>();
                    
                    // Water sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WaterConsumer("test-water", waterInfluxDBClient)));
                    }
                    
                    // Energy sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new EnergyConsumer("test-energy", energyInfluxDBClient)));
                    }
                    
                    // Waste bin sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WasteConsumer("test-waste", wasteInfluxDBClient)));
                    }
                    
                    // Traffic sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new TrafficConsumer("test-traffic", trafficInfluxDBClient)));
                    }
                    
                    // Wait for all tasks to complete
                    for (Future<?> future : futures) {
                        try {
                            future.get();
                        } catch (Exception e) {
                            System.err.println("Error in virtual thread execution: " + e.getMessage());
                        }
                    }
                }
                

            }
            case "none" -> System.out.println("Please specify mode of operation (server or client)");
            default -> System.out.println("Please specify a valid mode of operation (server or client)");
        }
    }
}