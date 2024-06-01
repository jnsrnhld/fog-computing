package com.fogcomputing;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class MessageReceiver implements Runnable {

	private final ZMQ.Socket responder;
	private final ZMQ.Poller poller;

	public MessageReceiver(ZMQ.Socket cloudResponder) {
		if (cloudResponder.getSocketType() != SocketType.REP) {
			throw new IllegalArgumentException("Invalid socket type: %s. Expecting SocketType.REP".formatted(cloudResponder.getSocketType()));
		}
		this.responder = cloudResponder;
		this.poller = ZContextProvider.getInstance().createPoller(1);
		this.poller.register(cloudResponder, ZMQ.Poller.POLLIN);
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			poller.poll();
			if (poller.pollin(0)) {
				String string = responder.recvStr();
				System.out.printf("Received request: [%s]\n", string);
				responder.send("OK");
			}
		}
	}
}
