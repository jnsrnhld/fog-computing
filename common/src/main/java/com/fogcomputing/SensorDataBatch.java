package com.fogcomputing;

import java.sql.Timestamp;
import java.util.List;

public record SensorDataBatch(
		List<SensorData> sensorData,
		int size,
		Timestamp timestamp
)
		implements Message {
	public SensorDataBatch(List<SensorData> sensorData, Timestamp timestamp) {
		this(sensorData, sensorData.size(), timestamp);
	}

	@Override
	public String toString() {
		return "SensorDataBatch: size = %s, data[Temp,Usage] = %s, timestamp = %s".formatted(size, sensorData, timestamp);
	}

	public SensorDataAggregation aggregate() {

		double averageTemperature = sensorData.stream()
											  .mapToInt(SensorData::temperature)
											  .average()
											  .orElse(0);
		double averageUsage = sensorData.stream()
										.mapToInt(SensorData::usage)
										.average()
										.orElse(0);

		return new SensorDataAggregation((int) averageTemperature, (int) averageUsage, timestamp);
	}
}
