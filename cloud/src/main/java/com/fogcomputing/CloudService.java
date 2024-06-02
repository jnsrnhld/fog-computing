package com.fogcomputing;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.SocketType;
import picocli.CommandLine;

public class CloudService implements Callable<Void> {

	private final CountDownLatch latch = new CountDownLatch(1);

	@CommandLine.Option(names = {"--port", "-p"}, description = "Port this CloudService listens to", defaultValue = "8080")
	private int cloudServicePort;

	@Override
	public Void call() throws InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(2); // assuming we need 2 threads in total
		startSensorDataServer(executor);

		latch.await(); // keep the main thread alive
		return null;
	}

	private void startSensorDataServer(ExecutorService executor) {
		Server sensorDataServer = new Server(cloudServicePort, SocketType.REP, (message) -> {
			try {
				SensorDataBatch sensorDataBatch = Message.deserialize(message, SensorDataBatch.class);
				System.out.printf("Received sensor data: [%s]\n", sensorDataBatch);
				return "OK";
			}
			catch (IOException | ClassNotFoundException e) {
				System.out.println("Error receiving sensor data: " + e.getMessage());
				return "ERR";
			}
		});
		ThreadUtils.registerShutdownHook(sensorDataServer::close);
		executor.submit(sensorDataServer);
	}

	public static void main (String[]args){
		int exitCode = new CommandLine(new CloudService()).execute(args);
		System.exit(exitCode);
	}
}
