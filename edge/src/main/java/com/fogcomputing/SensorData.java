package com.fogcomputing;


public record SensorData(
		int temperature,
		int usage
) {
	public SensorData(String temperature, String usage) {
		 this(Integer.parseInt(temperature), Integer.parseInt(usage));
	}

	@Override
	public String toString() {
		return "Temperature: " + temperature + ", Usage: " + usage;
	}
}
