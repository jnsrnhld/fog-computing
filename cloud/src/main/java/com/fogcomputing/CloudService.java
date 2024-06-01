package com.fogcomputing;

import java.util.concurrent.Callable;

import picocli.CommandLine;

public class CloudService implements Callable<Void> {

	@CommandLine.Option(names = {"--port", "-p"}, description = "Port this CloudService listens to", defaultValue = "8080")
	private int cloudServicePort;

	@Override
	public Void call() throws Exception {
		return null;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new CloudService()).execute(args);
		System.exit(exitCode);
	}

}
