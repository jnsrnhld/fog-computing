package com.fogcomputing;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSender implements Runnable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;

	@Override
	public void run() {
		while (true) {

			List<SensorData> batch = new ArrayList<>(DEFAULT_BATCH_SIZE);

			while (messageBuffer.peek() != null) {
				SensorData sensorData = messageBuffer.poll();
				batch.add(sensorData);
			}

			Timestamp currentDateTime = new Timestamp(Date.valueOf(LocalDate.now()).getTime());
			SensorDataBatch sensorDataBatch = new SensorDataBatch(batch, currentDateTime);
			System.out.printf("Batch ready to sent: %s\n", sensorDataBatch);
			// TODO send sensorDataBatch to Cloud service

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

}
