package com.fogcomputing;


import java.io.Serializable;
import java.sql.Timestamp;

public record SensorData(
		int temperature,
		int usage,
		Timestamp dateTime
)
		implements Serializable
{
	public SensorData(String temperature, String usage, Timestamp dateTime) {
		this(Integer.parseInt(temperature), Integer.parseInt(usage), dateTime);
	}

	@Override
	public String toString() {
		return "[%s,%s]".formatted(temperature, usage);
	}
}
