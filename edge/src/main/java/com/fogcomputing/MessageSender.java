package com.fogcomputing;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZMQ;

public class MessageSender implements Runnable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final ZMQ.Socket cloudSocket;
	private final List<SensorDataBatch> deadLetterBuffer;

	public MessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ZMQ.Socket cloudSocket) {
		this.messageBuffer = messageBuffer;
		this.cloudSocket = cloudSocket;
		this.deadLetterBuffer = new ArrayList<>();
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {

			if (!deadLetterBuffer.isEmpty()) {
				deadLetterBuffer.stream().filter(this::trySendToCloud).forEach(deadLetterBuffer::remove);
			}

			List<SensorData> batch = new ArrayList<>(DEFAULT_BATCH_SIZE);
			while (messageBuffer.peek() != null) {
				SensorData sensorData = messageBuffer.poll();
				batch.add(sensorData);
			}

			Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());
			SensorDataBatch sensorDataBatch = new SensorDataBatch(batch, currentDateTime);
			System.out.printf("Batch ready to sent: %s\n", sensorDataBatch);
//			trySendToCloud(sensorDataBatch); // not tested yet

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

	private boolean trySendToCloud(SensorDataBatch sensorDataBatch) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(bos))
		{
			oos.writeObject(sensorDataBatch);
			cloudSocket.send(bos.toByteArray());
			return true;
		}
		catch (Exception e) {
			System.err.println("Error sending message: " + e.getMessage());
			deadLetterBuffer.add(sensorDataBatch);
			return false;
		}
	}

}
