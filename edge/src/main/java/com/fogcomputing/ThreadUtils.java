package com.fogcomputing;

import java.util.concurrent.TimeUnit;


public class ThreadUtils {

	public static void sleep(int amount, TimeUnit timeUnit) {
		try {
			Thread.sleep(timeUnit.toMillis(amount));
		}
		catch (InterruptedException e) {
			System.err.printf("Interrupted while thread %s was sleeping%n", Thread.currentThread());
		}
	}

}
