package com.fogcomputing;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.SocketType;
import picocli.CommandLine;

public class EdgeService implements Callable<Void> {

	private final CountDownLatch latch = new CountDownLatch(1);

	@CommandLine.Parameters(index = "0", description = "Please provide a cloud server address", defaultValue = "localhost:8080")
	private String cloudServerAddress;

	@CommandLine.Parameters(index = "1", description = "Please provide a temperature sensor address", defaultValue = "*:5556")
	private String temperatureSensorAddress;

	@CommandLine.Parameters(index = "2", description = "Please provide a usage sensor address", defaultValue = "*:5555")
	private String usageSensorAddress;

	@Override
	public Void call() throws Exception {

		ThreadUtils.registerShutdownHook(ZContextProvider::close); // close ZContext during shutdown
		ExecutorService executor = Executors.newFixedThreadPool(3); // assuming we need 3 threads in total
		ConcurrentLinkedQueue<SensorData> messageBuffer = new ConcurrentLinkedQueue<>();

		startSensordataCollector(messageBuffer, executor);
		startMessageSender(messageBuffer, executor);

		latch.await(); // keep the main thread alive
		return null;
	}

	private void startSensordataCollector(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		SensorDataCollector sensorDataCollector = new SensorDataCollector(messageBuffer, temperatureSensorAddress, usageSensorAddress);
		executor.submit(sensorDataCollector);
	}

	private void startMessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		Client client = new Client(cloudServerAddress, SocketType.REQ);
		MessageSender messageSender = new MessageSender(messageBuffer, client);
		ThreadUtils.registerShutdownHook(client::close);
		executor.submit(messageSender);
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new EdgeService()).execute(args);
		System.exit(exitCode);
	}

}
