package util;

import java.time.Instant;

public class ENIntervalNumberUtils {


	public static long getUnixTime(long interval) {
		return interval * 600;
	}

	public static Instant getUnixTimeInstant(long interval) {
		return Instant.ofEpochSecond(getUnixTime(interval));
	}
}
