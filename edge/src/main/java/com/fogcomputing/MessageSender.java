package com.fogcomputing;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class MessageSender implements Runnable, Closeable {

	private static final int DEFAULT_BATCH_SIZE = 15; // we send every 15 seconds, sensor data is written/collected every second
	private static final int POLLING_TIMEOUT = 1000;

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final ZMQ.Socket cloudSocket;
	private final ZMQ.Poller poller;

	public MessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ZMQ.Socket cloudSocket) {
		if (cloudSocket.getSocketType() != SocketType.REQ) {
			throw new IllegalArgumentException("Invalid socket type: %s. Expecting SocketType.REQ".formatted(cloudSocket.getSocketType()));
		}
		this.messageBuffer = messageBuffer;
		this.cloudSocket = cloudSocket;
		this.poller = ZContextProvider.getInstance().createPoller(1);
		this.poller.register(cloudSocket);
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
			trySendToCloud(sensorDataBatch);

			ThreadUtils.sleep(15, TimeUnit.SECONDS);
		}
	}

	@Override
	public void close() {
		poller.unregister(cloudSocket);
		poller.close();
		cloudSocket.close();
	};

	private void trySendToCloud(SensorDataBatch sensorDataBatch) {
		try {
			System.out.printf("Trying to sent batch: %s\n", sensorDataBatch);
			cloudSocket.send(sensorDataBatch.serialize());
			while (!Thread.currentThread().isInterrupted()) {
				poller.poll(POLLING_TIMEOUT);
				if (poller.pollin(0)) {
					String response = cloudSocket.recvStr();
					System.out.println("Got response from cloud server: " + response);
					return;
				}
			}
		}
		catch (IOException e) {
			System.err.printf("Error sending message: %s. Possibly due to a serialization error?%n", e.getMessage());
			System.exit(1);
		}
		catch (Exception e) {
			System.err.printf("Unknown error sending message: %s.", e.getMessage());
		}
	}

}
