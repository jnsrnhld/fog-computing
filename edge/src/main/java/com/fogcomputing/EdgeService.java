package com.fogcomputing;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import picocli.CommandLine;

public class EdgeService implements Callable<Void> {

	private final CountDownLatch latch = new CountDownLatch(1);

	@CommandLine.Parameters(index = "0", description = "Please provide a cloud server address")
	private String cloudServerAddress;

	@CommandLine.Option(names = {"--port", "-p"}, description = "Port this EdgeService listens to", defaultValue = "8081")
	private int edgeServicePort;

	@Override
	public Void call() throws Exception {

		registerShutdownHook(ZContextProvider::close); // close ZContext during shutdown
		ExecutorService executor = Executors.newFixedThreadPool(3); // assuming we need 3 threads in total
		ConcurrentLinkedQueue<SensorData> messageBuffer = new ConcurrentLinkedQueue<>();

		startSensordataCollector(messageBuffer, executor);
		startMessageSender(messageBuffer, executor);

		latch.await(); // keep the main thread alive
		return null;
	}

	private static void startSensordataCollector(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		SensorDataCollector sensorDataCollector = new SensorDataCollector(messageBuffer);
		executor.submit(sensorDataCollector);
	}

	private void startMessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		ZMQ.Socket cloudSocket = ZContextProvider.getInstance().createSocket(SocketType.REQ);
		registerShutdownHook(cloudSocket::close);
		cloudSocket.connect("tcp://%s".formatted(cloudServerAddress));
		MessageSender messageSender = new MessageSender(messageBuffer, cloudSocket);
		executor.submit(messageSender);
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new EdgeService()).execute(args);
		System.exit(exitCode);
	}

	private static void registerShutdownHook(Runnable runnable) {
		Thread printingHook = new Thread(runnable);
		Runtime.getRuntime().addShutdownHook(printingHook);
	}
}
