package util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ENIntervalNumberUtils {


	public static long getUnixTime(long interval) {
		return interval * 600;
	}

	public static Instant getUnixTimeInstant(long interval) {
		return Instant.ofEpochSecond(getUnixTime(interval));
	}

	public static LocalDateTime getLocalDate(long interval) {
		return LocalDateTime.ofInstant(getUnixTimeInstant(interval), ZoneId.of("Z"));
	}

}
