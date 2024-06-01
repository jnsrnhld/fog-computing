package com.fogcomputing;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import picocli.CommandLine;

public class CloudService implements Callable<Void> {

	@CommandLine.Option(names = {"--port", "-p"}, description = "Port this CloudService listens to", defaultValue = "8080")
	private int cloudServicePort;

	@Override
	public Void call() throws Exception {
		try (ZMQ.Socket responder = ZContextProvider.getInstance().createSocket(SocketType.REP)) {

			boolean connected = false;
			while (!connected) {
				connected = responder.connect("tcp://localhost:%s".formatted(cloudServicePort));
				System.out.printf("Was not able to init cloud service on port %s...%n", cloudServicePort);
				ThreadUtils.sleep(5, TimeUnit.SECONDS);
			}
			System.out.printf("Cloud service listening on port %s...%n", cloudServicePort);

			while (!Thread.currentThread().isInterrupted()) {
				//  Wait for next request from client
				String string = responder.recvStr(0);
				System.out.printf("Received request: [%s]\n", string);
				responder.send("OK");
			}
		}
		return null;
	}

	public static void main (String[]args){
		int exitCode = new CommandLine(new CloudService()).execute(args);
		System.exit(exitCode);
	}
}
