package com.fogcomputing;

import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSender implements Runnable {

	private final ConcurrentLinkedQueue<SensorData> outboundMessages;

	@Override
	public void run() {
		while (true) {
			while (outboundMessages.peek() != null) {
				SensorData sensorData = outboundMessages.poll();
				System.out.println("Read message from queue: " + sensorData);
				// TODO send message to Cloud service
			}
		}
	}
}
