package com.fogcomputing;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.ZMQ;

public class EdgeService {

	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(3); // assuming we need 3 threads in total
		ConcurrentLinkedQueue<SensorData> messageBuffer = new ConcurrentLinkedQueue<>();

		SensorDataCollector sensorDataCollector = new SensorDataCollector(messageBuffer);
		executor.submit(sensorDataCollector);

		ZMQ.Socket cloudSocket = null; // just a dummy for now
		MessageSender messageSender = new MessageSender(messageBuffer, cloudSocket);
		executor.submit(messageSender);
	}

}
