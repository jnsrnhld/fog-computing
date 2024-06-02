package com.fogcomputing;

import java.sql.Timestamp;

public record SensorDataAggregation(
		int averageTemperature,
		int averageUsage,
		Timestamp timestamp
)
		implements Message
{
	@Override
	public String toString() {
		return "[SensorDataAggregation %s [averageTemperature=%d, averageUsage=%d]]".formatted(timestamp, averageTemperature, averageUsage);
	}
}
