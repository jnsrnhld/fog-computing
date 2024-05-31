package com.fogcomputing;

import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSender implements Runnable {

	private final ConcurrentLinkedQueue<String> outboundMessages;

	@Override
	public void run() {
		while (true) {
			while (outboundMessages.peek() != null) {
				String message = outboundMessages.poll();
				System.out.println("Read message from queue: " + message);
				// TODO send message to Cloud service
			}
		}
	}
}
