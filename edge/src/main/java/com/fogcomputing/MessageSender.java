package com.fogcomputing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class MessageSender implements Runnable {

	private static final int BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final Client client;

	public MessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, Client client) {
		this.messageBuffer = messageBuffer;
		this.client = client;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {

			List<SensorData> batch = new ArrayList<>(BATCH_SIZE);
			while (batch.size() < BATCH_SIZE && messageBuffer.peek() != null) {
				SensorData sensorData = messageBuffer.poll();
				batch.add(sensorData);
			}

			// send 1-x batches until buffer is empty, than sleep for 15 seconds
			// this results in sending a batch of 15 SensorData every 15 seconds if server is working properly
			if (batch.isEmpty()) {
				ThreadUtils.sleep(15, TimeUnit.SECONDS);
				continue;
			}

			SensorDataBatch sensorDataBatch = SensorDataBatch.of(batch);
			byte[] response = client.trySend(sensorDataBatch);
			handleResponse(response);
		}
	}

	private static void handleResponse(byte[] response) {
		try {
			SensorDataAggregation sensorDataAggregation = Message.deserialize(response, SensorDataAggregation.class);
			System.out.printf("Received aggregated sensor data %s\n", sensorDataAggregation);
		}
		catch (IOException | ClassNotFoundException e) {
			System.err.println("Interval error processing aggregated sensor data");
			System.exit(1);
		}
	}

}
