package com.fogcomputing;

import java.io.Closeable;
import java.io.IOException;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class Client implements Closeable {

	private static final int POLLING_TIMEOUT = 1000;

	private final ZMQ.Socket cloudSocket;
	private final ZMQ.Poller poller;

	public Client(String address, SocketType socketType) {
		this.cloudSocket = ZContextProvider.getInstance().createSocket(socketType);
		this.cloudSocket.connect("tcp://%s".formatted(address));
		this.poller = ZContextProvider.getInstance().createPoller(1);
		this.poller.register(this.cloudSocket, ZMQ.Poller.POLLIN);
	}

	public byte[] trySend(Message message) {
		try {
			System.out.printf("Trying to sent data: %s\n", message);
			cloudSocket.send(message.serialize());
			while (!Thread.currentThread().isInterrupted()) {
				poller.poll(POLLING_TIMEOUT);
				if (poller.pollin(0)) {
					return cloudSocket.recv();
				}
			}
		}
		catch (IOException e) {
			System.err.printf("Serialization error occurred. %s/n", e.getMessage());
			System.exit(1);
		}
		catch (Exception e) {
			System.err.printf("Unknown error sending message: %s.", e.getMessage());
			System.exit(1);
		}
		// unreachable
		return null;
	}

	@Override
	public void close() {
		poller.unregister(cloudSocket);
		poller.close();
		cloudSocket.close();
	}
}
