/**
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dev.kiretori.smartcity;
import java.time.LocalDateTime;
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
import dev.kiretori.smartcity.producers.SimulationTimeManager;
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
                
                int totalSensors = threadNum * 4;

                // Create simulation time manager
                LocalDateTime simulationStart = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
                int timeIncrementHours = 1; // Advance by 1 hour each time all sensors send data
                SimulationTimeManager timeManager = new SimulationTimeManager(simulationStart, timeIncrementHours, totalSensors);

                System.out.println("Starting simulation with " + totalSensors + " sensors at: " + timeManager.getCurrentTime());


                // Using virtual threads executor
                try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    List<Future<?>> futures = new ArrayList<>();
                    
                    // Water sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WaterProducer(timeManager)));
                    }
                    
                    // Energy sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new EnergyProducer(timeManager)));
                    }
                    
                    // Waste bin sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WasteBinProducer(timeManager)));
                    }
                    
                    // Traffic sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new TrafficProducer(timeManager)));
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

                InfluxDBClient waterInfluxDBClient = InfluxDBService.getInfluxClient("water");
                InfluxDBClient wasteInfluxDBClient = InfluxDBService.getInfluxClient("waste");
                InfluxDBClient trafficInfluxDBClient = InfluxDBService.getInfluxClient("traffic");
                InfluxDBClient energyInfluxDBClient = InfluxDBService.getInfluxClient("energy");


                try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    List<Future<?>> futures = new ArrayList<>();
                    
                    // Water sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WaterConsumer("water", waterInfluxDBClient)));
                    }
                    
                    // Energy sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new EnergyConsumer("energy", energyInfluxDBClient)));
                    }
                    
                    // Waste bin sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new WasteConsumer("waste", wasteInfluxDBClient)));
                    }
                    
                    // Traffic sensors
                    for (int i = 0; i < threadNum; i++) {
                        futures.add(executor.submit(new TrafficConsumer("traffic", trafficInfluxDBClient)));
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