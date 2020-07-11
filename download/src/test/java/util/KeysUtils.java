package util;

import com.google.protobuf.ByteString;
import protobuf.TemporaryExposureKey;
import protobuf.TemporaryExposureKeyExport;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static protobuf.TemporaryExposureKey.ReportType.SELF_REPORT;

public class KeysUtils {

	private static Random r = ThreadLocalRandom.current();

	private static String genRandomHexString(int numBytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numBytes; i++) {
			sb.append(String.format("%02x", (byte)r.nextInt()));
		}


		return sb.toString();
	}



	public static void main(String[] args) {
		System.out.println(genRandomHexString(16));
	}


	public static TemporaryExposureKey genTemporaryExposureKey() {
		return TemporaryExposureKey.newBuilder()
			   .setKeyData(ByteString.copyFromUtf8(genRandomHexString(16)))
			   .setTransmissionRiskLevel(0)
			   .setRollingStartIntervalNumber(r.nextInt(200))
			   .setReportType(SELF_REPORT)
			   .setDaysSinceOnsetOfSymptoms(0)
				.build();
	}


	public static TemporaryExposureKeyExport genTemporaryExposureKeyExport(int numKeys, int numRevisedKeys) {
		Instant instant = Instant.now().minus(r.nextInt(20_000), ChronoUnit.DAYS);
		final List<TemporaryExposureKey> keys = Stream.generate(KeysUtils::genTemporaryExposureKey)
														 .limit(numKeys)
														 .collect(Collectors.toList());
		final List<TemporaryExposureKey> revisedKeys = Stream.generate(KeysUtils::genTemporaryExposureKey)
													  .limit(numRevisedKeys)
													  .collect(Collectors.toList());

		return TemporaryExposureKeyExport.newBuilder()
				 .setStartTimestamp(instant.getEpochSecond())
				 .setEndTimestamp(instant.plus(2, ChronoUnit.DAYS).getEpochSecond())
				 .setRegion("DE")
				 .addAllKeys(keys)
				 .addAllRevisedKeys(revisedKeys)
				.build();
	}

}
