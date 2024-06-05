package com.fogcomputing;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@RequiredArgsConstructor
public class SensorDataCollector implements Runnable {

	private static final int USAGE_TOPIC_PORT = 5555;
	private static final int TEMPERATURE_TOPIC_PORT = 5556;
	private static final String USAGE_TOPIC = "USAGE";
	private static final String TEMPERATURE_TOPIC = "TEMPERATURE";

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;

	@Override
	public void run() {
		try (ZMQ.Socket usageSub = createSubscriber(USAGE_TOPIC_PORT, USAGE_TOPIC);
			 ZMQ.Socket temperatureSub = createSubscriber(TEMPERATURE_TOPIC_PORT, TEMPERATURE_TOPIC);)
		{
			while (!Thread.currentThread().isInterrupted()) {
				// we do sync read of both sensors
				String usageData = usageSub.recvStr().substring(USAGE_TOPIC.length() + 1);
				String temperatureData = temperatureSub.recvStr().substring(TEMPERATURE_TOPIC.length() + 1);
				Timestamp dateTime = new Timestamp(System.currentTimeMillis());
				messageBuffer.offer(new SensorData(usageData, temperatureData, dateTime));
			}
		}
	}

	private static ZMQ.Socket createSubscriber(int port, String topic) {
		ZContext context = ZContextProvider.getInstance();
		ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
		subscriber.connect("tcp://*:%d".formatted(port));
		subscriber.subscribe(topic.getBytes());
		return subscriber;
	}
}
