package com.fogcomputing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public record SensorDataBatch(
		List<SensorData> sensorData,
		int size,
		Timestamp timestamp
)
		implements Serializable
{
	public SensorDataBatch(List<SensorData> sensorData, Timestamp timestamp) {
		this(sensorData, sensorData.size(), timestamp);
	}

	@Override
	public String toString() {
		return "SensorDataBatch: size = %s, data[Temp,Usage] = %s, timestamp = %s".formatted(size, sensorData, timestamp);
	}

	public byte[] serialize() throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(bos))
		{
			oos.writeObject(this);
			return bos.toByteArray();
		}
	}

	public static SensorDataBatch deserialize(byte[] data) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bos = new ByteArrayInputStream(data);
			 ObjectInputStream oos = new ObjectInputStream(bos))
		{
			return (SensorDataBatch) oos.readObject();
		}
	}
}
