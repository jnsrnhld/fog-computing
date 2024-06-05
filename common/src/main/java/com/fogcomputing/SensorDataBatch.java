package com.fogcomputing;

import java.sql.Timestamp;
import java.util.List;

public record SensorDataBatch(
		List<SensorData> sensorData,
		int size,
		Timestamp first,
		Timestamp last

)
		implements Message
{
	public static SensorDataBatch of(List<SensorData> sensorData) {

		Timestamp first;
		Timestamp last;
		if (sensorData.isEmpty()) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			first = now;
			last = now;
		}
		else {
			first = sensorData.get(0).dateTime();
			last = sensorData.get(sensorData.size() - 1).dateTime();
		}

		return new SensorDataBatch(sensorData, sensorData.size(), first, last);
	}

	@Override
	public String toString() {
		return "SensorDataBatch: size = %s, time range = [%s-%s] data[Temp,Usage] = %s".formatted(
				size,
				TimestampFormatter.timeOnly(first),
				TimestampFormatter.timeOnly(last),
				sensorData
		);
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

		return new SensorDataAggregation((int) averageTemperature, (int) averageUsage, first, last);
	}
}
