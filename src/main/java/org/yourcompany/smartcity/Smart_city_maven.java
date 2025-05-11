/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.smartcity;
import java.util.ArrayList;
import java.util.List;

import org.yourcompany.smartcity.consumers.Consumer;
import org.yourcompany.smartcity.producers.EnergyProducer;
import org.yourcompany.smartcity.producers.TrafficProducer;
import org.yourcompany.smartcity.producers.WasteBinProducer;
import org.yourcompany.smartcity.producers.WaterProducer;

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

                List<Thread> producerThreads = new ArrayList<>(); 
                // Water sensors 
                for (int i = 0; i < threadNum; i++) {
                    Thread producerThread = new Thread(new WaterProducer());
                    producerThreads.add(producerThread);
                    producerThread.start();
                }
                
                // Energy sensors
                for (int i = 0; i < threadNum; i++) {
                    Thread producerThread = new Thread(new EnergyProducer());
                    producerThreads.add(producerThread);
                    producerThread.start();
                }

                // Waste bin sensors
                for (int i = 0; i < threadNum; i++) {
                    Thread producerThread = new Thread(new WasteBinProducer());
                    producerThreads.add(producerThread);
                    producerThread.start();
                }

                // Traffic sensors
                for (int i = 0; i < threadNum; i++) {
                    Thread producerThread = new Thread(new TrafficProducer());
                    producerThreads.add(producerThread);
                    producerThread.start();
                }
                
                for (Thread t: producerThreads) {
                    try {
                        t.join();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            case "client" -> {
                System.out.println("Running as client");
                Thread consumerThread = new Thread(new Consumer("test-energy"));
                consumerThread.start();
                 try {
                    consumerThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            case "none" -> System.out.println("Please specify mode of operation (server or client)");
            default -> System.out.println("Please specify a valid mode of operation (server or client)");
        }
        

    }
}
