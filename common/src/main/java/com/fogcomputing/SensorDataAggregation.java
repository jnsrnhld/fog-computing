package com.fogcomputing;

import java.sql.Timestamp;

public record SensorDataAggregation(
		int averageTemperature,
		int averageUsage,
		Timestamp first,
		Timestamp last
)
		implements Message
{
	@Override
	public String toString() {
		return "SensorDataAggregation [%s - %s] [averageTemperature=%d, averageUsage=%d]".formatted(
				TimestampFormatter.timeOnly(first),
				TimestampFormatter.timeOnly(last),
				averageTemperature,
				averageUsage
		);
	}
}
