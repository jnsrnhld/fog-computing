package com.fogcomputing;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimestampFormatter {

	public static String timeOnly(Timestamp timestamp) {
		return timestamp.toInstant()
						.atOffset(ZoneOffset.UTC )
						.format(DateTimeFormatter.ofPattern("HH:mm:ss" ));
	}

}
