package com.fogcomputing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.zeromq.ZContext;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZContextProvider {

	private static ZContext instance;

	public static ZContext getInstance() {
		if (instance == null) {
			instance = new ZContext();
		}
		return instance;
	}

	public static void close() {
		if (instance != null) {
			instance.close();
		}
	}

}
