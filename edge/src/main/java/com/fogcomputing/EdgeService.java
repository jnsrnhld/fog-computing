package com.fogcomputing;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import picocli.CommandLine;

public class EdgeService implements Callable<Void> {

	@CommandLine.Parameters(index = "0", description = "Please provide a cloud server address")
	private String cloudServerAddress;

	@CommandLine.Option(names = {"--port", "-p"}, description = "Port this EdgeService listens to", defaultValue = "8081")
	private int edgeServicePort;

	@Override
	public Void call() throws Exception {

		registerShutdownHook(); // close ZContext during shutdown
		ExecutorService executor = Executors.newFixedThreadPool(3); // assuming we need 3 threads in total
		ConcurrentLinkedQueue<SensorData> messageBuffer = new ConcurrentLinkedQueue<>();

		startSensordataCollector(messageBuffer, executor);
		startMessageSender(messageBuffer, executor);
//		startMessageReceiver(executor);

		return null;
	}

	private static void startSensordataCollector(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		SensorDataCollector sensorDataCollector = new SensorDataCollector(messageBuffer);
		executor.submit(sensorDataCollector);
	}

	private void startMessageSender(ConcurrentLinkedQueue<SensorData> messageBuffer, ExecutorService executor) {
		try (ZMQ.Socket cloudSocket = ZContextProvider.getInstance().createSocket(SocketType.REQ))
		{
			boolean connected = false;
			while (!connected) {
				connected = cloudSocket.connect("tcp://%s".formatted(cloudServerAddress));
				System.out.printf("Was not able to connect to cloud server on %s...%n", cloudServerAddress);
				ThreadUtils.sleep(5, TimeUnit.SECONDS);
			}
			MessageSender messageSender = new MessageSender(messageBuffer, cloudSocket);
			executor.submit(messageSender);
		}
	}

	private void startMessageReceiver(ExecutorService executor) {
		try (ZMQ.Socket cloudSocket = ZContextProvider.getInstance().createSocket(SocketType.REP))
		{
			boolean connected = false;
			while (!connected) {
				connected = cloudSocket.connect("tcp://localhost:%s".formatted(edgeServicePort));
				System.out.println("Was not able to start message receiver on localhost port %s...");
				ThreadUtils.sleep(5, TimeUnit.SECONDS);
			}
			MessageReceiver messageReceiver = new MessageReceiver(cloudSocket);
			executor.submit(messageReceiver);
		}
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new EdgeService()).execute(args);
		System.exit(exitCode);
	}

	private static void registerShutdownHook() {
		Thread printingHook = new Thread(ZContextProvider::close);
		Runtime.getRuntime().addShutdownHook(printingHook);
	}
}
