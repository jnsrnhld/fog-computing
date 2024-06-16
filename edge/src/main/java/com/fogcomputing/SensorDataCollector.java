package com.fogcomputing;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@RequiredArgsConstructor
public class SensorDataCollector implements Runnable {

	private static final String USAGE_TOPIC = "USAGE";
	private static final String TEMPERATURE_TOPIC = "TEMPERATURE";

	private final ConcurrentLinkedQueue<SensorData> messageBuffer;
	private final String temperatureSensorAdress;
	private final String usageSensorAddress;

	@Override
	public void run() {
		try (ZMQ.Socket usageSub = createSubscriber(USAGE_TOPIC);
			 ZMQ.Socket temperatureSub = createSubscriber(TEMPERATURE_TOPIC);)
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

	private ZMQ.Socket createSubscriber(String topic) {
		System.out.println(usageSensorAddress);
		ZContext context = ZContextProvider.getInstance();
		ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
		if(topic  == USAGE_TOPIC) {
			System.out.println(usageSensorAddress);
			subscriber.connect("tcp://%s".formatted(usageSensorAddress));
		} else {
			subscriber.connect("tcp://%s".formatted(temperatureSensorAdress));
		}
		subscriber.subscribe(topic.getBytes());
		return subscriber;
	}
}
