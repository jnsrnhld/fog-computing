package com.fogcomputing;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EdgeService {

	public static void main(String[] args) {

		// assuming we need 3 threads in total
		ExecutorService executor = Executors.newFixedThreadPool(3);

		// using String and not a custom object for a first draft
		ConcurrentLinkedQueue<SensorData> messageBuffer = new ConcurrentLinkedQueue<>();

		SensorDataCollector sensorDataCollector = new SensorDataCollector(messageBuffer);
		executor.submit(sensorDataCollector);

		MessageSender messageSender = new MessageSender(messageBuffer);
		executor.submit(messageSender);
	}

}
