import download.de.DE_Distribution;
import org.junit.jupiter.api.Test;
import protobuf.TemporaryExposureKey;
import util.ENIntervalNumberUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;

class DE_DistributionTest_old {



	private final static DE_Distribution dt = new DE_Distribution();


	private static Stream<TemporaryExposureKey> getDefaultKeys() {
		return dt.getAllAvailableKeys().stream()
				 .flatMap(e -> e.getKeysList().stream());
	}

	@Test
	public void transmissionRiskLevelStatistics() {
//		DE_Distribution dt = new DE_Distribution();
//		Optional<TemporaryExposureKeyExport> teks;
//		teks = dt.extractKeysFromZipFile(Paths.get(""));
//		final long[] collect = teks.orElseThrow().getKeysList().stream()
//								   .mapToInt(TemporaryExposureKey::getTransmissionRiskLevel)
//								   .collect(() -> new long[9], (longs, value) -> longs[value]++, (arr1, arr2) -> {
//									   for (int i = 0; i < arr1.length; i++) {
//										   arr1[i] += arr2[i];
//									   }
//								   });
//		for (int i = 0; i < collect.length; i++) {
//			System.out.println(String.format("%d: %d", i, collect[i]));
//		}


		//alt:
		final Map<Integer, Long> map = getDefaultKeys()
										 .map(TemporaryExposureKey::getTransmissionRiskLevel)
										 .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> System.out.printf("%d: %d\n", entry.getKey(), entry.getValue()));
	}



	@Test
	public void rollingStartIntervalNumberStatistics() {
		LocalTime midnight = LocalTime.of(0,0);
		final Map<Instant, Long> map = getDefaultKeys()
														   .map(TemporaryExposureKey::getRollingStartIntervalNumber)
														   .map(ENIntervalNumberUtils::getUnixTimeInstant)

														   .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((entry) -> System.out.printf("%s: %d\n", entry.getKey(), entry.getValue()));

		getDefaultKeys()
						.map(TemporaryExposureKey::getRollingStartIntervalNumber)
						.map(ENIntervalNumberUtils::getUnixTimeInstant)
						.map(i -> LocalDateTime.ofInstant(i, UTC))
						.map(l -> {
							if(l.toLocalTime().equals(midnight)){
								return l.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
							}else {
								return l.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
							}
						})
						.forEach(System.out::println);



	}

}