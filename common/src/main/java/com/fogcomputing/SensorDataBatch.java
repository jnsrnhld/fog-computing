package com.fogcomputing;

import java.sql.Timestamp;
import java.util.List;

public record SensorDataBatch(
		List<SensorData> sensorData,
		int size,
		Timestamp timestamp
)
		implements Message
{
	public SensorDataBatch(List<SensorData> sensorData, Timestamp timestamp) {
		this(sensorData, sensorData.size(), timestamp);
	}

	@Override
	public String toString() {
		return "SensorDataBatch: size = %s, data[Temp,Usage] = %s, timestamp = %s".formatted(size, sensorData, timestamp);
	}

}
