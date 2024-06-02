package com.fogcomputing;

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
			client.trySend(sensorDataBatch);

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

}
