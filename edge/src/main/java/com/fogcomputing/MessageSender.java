package com.fogcomputing;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class MessageSender implements Runnable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final List<SensorDataBatch> deadLetterBuffer;
	private final ZMQ.Socket cloudSocket;
	private final ZMQ.Poller poller;

	public MessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ZMQ.Socket cloudSocket) {
		if (cloudSocket.getSocketType() != SocketType.REQ) {
			throw new IllegalArgumentException("Invalid socket type: %s. Expecting SocketType.REQ".formatted(cloudSocket.getSocketType()));
		}
		this.messageBuffer = messageBuffer;
		this.deadLetterBuffer = new ArrayList<>();
		this.cloudSocket = cloudSocket;
		this.poller = ZContextProvider.getInstance().createPoller(1);
		this.poller.register(cloudSocket);
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
			trySendToCloud(sensorDataBatch);

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

	private boolean trySendToCloud(SensorDataBatch sensorDataBatch) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(bos))
		{
			int tries = 0;
			while (tries < 3) {
				tries++;
				// for now, I'm sending strings and skip serialization
				oos.writeObject(sensorDataBatch);
				cloudSocket.send(sensorDataBatch.toString());
				poller.poll();
				if (poller.pollin(0)) {
					String response = cloudSocket.recvStr();
					System.out.println("Got response from cloud server: " + response);
					return true;
				}
				ThreadUtils.sleep(1, TimeUnit.SECONDS);
			}
			deadLetterBuffer.add(sensorDataBatch);
			return false;
		}
		catch (Exception e) {
			System.err.println("Error sending message: " + e.getMessage());
			deadLetterBuffer.add(sensorDataBatch);
			return false;
		}
	}

}
