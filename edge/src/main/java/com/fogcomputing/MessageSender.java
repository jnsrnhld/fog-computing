package com.fogcomputing;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class MessageSender implements Runnable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second
	private static final int POLLING_TIMEOUT = 1000;
	private static final int MAX_TRIES = 3;

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
			trySendToCloud(sensorDataBatch);

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

	private boolean trySendToCloud(SensorDataBatch sensorDataBatch) {
		System.out.printf("Trying to sent batch: %s\n", sensorDataBatch);
		try {
			int tries = 0;
			while (tries < MAX_TRIES) {
				tries++;
				cloudSocket.send(sensorDataBatch.serialize());
				poller.poll(POLLING_TIMEOUT);
				if (poller.pollin(0)) {
					String response = cloudSocket.recvStr();
					System.out.println("Got response from cloud server: " + response);
					return true;
				}
			}
			return handleFailedSend(sensorDataBatch);
		}
		catch (IOException e) {
			System.err.printf("Error sending message: %s. Possibly due to a serialization error?%n", e.getMessage());
			return handleFailedSend(sensorDataBatch);
		}
		catch (ZMQException e) {
			System.err.printf("Error sending message: %s. Probably server did not respond within %d tries...", e.getMessage(), MAX_TRIES);
			return handleFailedSend(sensorDataBatch);
		}
	}

	private boolean handleFailedSend(SensorDataBatch sensorDataBatch) {
		deadLetterBuffer.add(sensorDataBatch);
		return false;
	}

}
