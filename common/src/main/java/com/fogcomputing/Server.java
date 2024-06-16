package com.fogcomputing;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class Server implements Runnable, Closeable {

	private final Function<byte[], Message> answer;
	private final ZMQ.Socket server;
	private final int port;

	public Server(int port, SocketType socketType, Function<byte[], Message> answer) {
		this.server = ZContextProvider.getInstance().createSocket(socketType);
		this.port = port;
		this.answer = answer;
	}

	@Override
	public void run() {
		server.bind("tcp://*:%s".formatted(port));
		System.out.printf("Server listening on port %s...%n", port);

		while (!Thread.currentThread().isInterrupted()) {
			//  Wait for next message from client
			byte[] message = server.recv(0);
			try {
				server.send(answer.apply(message).serialize());
			}
			catch (IOException e) {
				System.err.printf("Error serializing message: %s%n", e.getMessage());
			}
		}
	}

	@Override
	public void close() {
		server.close();
	}

}
