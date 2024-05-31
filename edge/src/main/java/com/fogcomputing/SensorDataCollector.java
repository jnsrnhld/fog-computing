package com.fogcomputing;

import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@RequiredArgsConstructor
public class SensorDataCollector implements Runnable {

	private static final String USAGE_TOPIC_PORT = "5555";
	private static final String TEMPERATURE_TOPIC_PORT = "5556";
	private static final String USAGE_TOPIC = "USAGE";
	private static final String TEMPERATURE_TOPIC = "TEMPERATURE";

	private final ConcurrentLinkedQueue<String> messageSink;

	@Override
	public void run() {
		try (ZContext context = new ZContext()) {

			ZMQ.Socket usageSub = context.createSocket(SocketType.SUB);
			usageSub.connect("tcp://*:%s".formatted(USAGE_TOPIC_PORT));
			usageSub.subscribe(USAGE_TOPIC.getBytes());

			ZMQ.Socket temperatureSub = context.createSocket(SocketType.SUB);
			temperatureSub.connect("tcp://*:%s".formatted(TEMPERATURE_TOPIC_PORT));
			temperatureSub.subscribe(TEMPERATURE_TOPIC.getBytes());

			while (true) {
				String usageData = usageSub.recvStr();
				String temperatureData = temperatureSub.recvStr();
				messageSink.offer(usageData);
				messageSink.offer(temperatureData);
			}
		}
	}
}
