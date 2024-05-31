package com.fogcomputing;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class EdgeService {

	private static final String USAGE_TOPIC = "USAGE";
	private static final String TEMPERATURE_TOPIC = "TEMPERATURE";

	public static void main(String[] args) {
		try (ZContext context = new ZContext()) {

			ZMQ.Socket usageSub = context.createSocket(SocketType.SUB);
			usageSub.connect("tcp://*:5555");
			usageSub.subscribe(USAGE_TOPIC.getBytes());

			ZMQ.Socket temperatureSub = context.createSocket(SocketType.SUB);
			temperatureSub.connect("tcp://*:5556");
			temperatureSub.subscribe(TEMPERATURE_TOPIC.getBytes());

			while (true) {
				String usageData = usageSub.recvStr();
				String temperatureData = temperatureSub.recvStr();
				System.out.println(usageData);
				System.out.println(temperatureData);
			}
		}
	}
}
