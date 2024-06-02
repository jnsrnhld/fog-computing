package com.fogcomputing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface Message extends Serializable {

	 default byte[] serialize() throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(bos))
		{
			oos.writeObject(this);
			return bos.toByteArray();
		}
	}

	static <T> T deserialize(byte[] data, Class<T> type) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bos = new ByteArrayInputStream(data);
			 ObjectInputStream oos = new ObjectInputStream(bos))
		{
			return type.cast(oos.readObject());
		}
	}

}
