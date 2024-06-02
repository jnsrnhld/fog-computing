package com.fogcomputing;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class MessageSender implements Runnable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final Client client;

	public MessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, Client client) {
		this.messageBuffer = messageBuffer;
		this.client = client;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {

			List<SensorData> batch = new ArrayList<>(DEFAULT_BATCH_SIZE);
			while (messageBuffer.peek() != null) {
				SensorData sensorData = messageBuffer.poll();
				batch.add(sensorData);
			}

			Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());
			SensorDataBatch sensorDataBatch = new SensorDataBatch(batch, currentDateTime);
			byte[] response = client.trySend(sensorDataBatch);
			handleResponse(response);

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
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
