package com.fogcomputing;


import java.io.Serializable;

public record SensorData(
		int temperature,
		int usage
)
		implements Serializable
{
	public SensorData(String temperature, String usage) {
		this(Integer.parseInt(temperature), Integer.parseInt(usage));
	}

	@Override
	public String toString() {
		return "[%s,%s]".formatted(temperature, usage);
	}
}
