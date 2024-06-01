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
				connected = responder.bind("tcp://localhost:%s".formatted(cloudServicePort));
				System.out.printf("Was not able to init cloud service on port %s...%n", cloudServicePort);
				ThreadUtils.sleep(5, TimeUnit.SECONDS);
			}
			System.out.printf("Cloud service listening on port %s...%n", cloudServicePort);

			while (!Thread.currentThread().isInterrupted()) {
				//  Wait for next message from client
				byte[] message = responder.recv(0);
				SensorDataBatch sensorDataBatch = SensorDataBatch.deserialize(message);
				System.out.printf("Received sensor data: [%s]\n", sensorDataBatch);
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
