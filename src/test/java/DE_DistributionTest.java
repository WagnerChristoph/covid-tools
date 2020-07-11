import distribution.de.DE_Distribution;
import org.junit.jupiter.api.Test;
import protobuf.TemporaryExposureKey;
import protobuf.TemporaryExposureKeyExport;
import util.ENIntervalNumberUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DE_DistributionTest {

	//todo clean-up
	private final static Path defaultKeyExportFile = Paths.get("");

	private static List<TemporaryExposureKey> getKeysFromFile(Path p) {
		return new DE_Distribution().extractKeysFromZipFile(p).orElseThrow().getKeysList();
	}

	private static List<TemporaryExposureKey> getDefaultKeys() {
		return getKeysFromFile(defaultKeyExportFile);
	}

	@Test
	public void readKeysFromFile(){
		DE_Distribution dt = new DE_Distribution();
		Optional<TemporaryExposureKeyExport> teks;
		teks = dt.extractKeysFromZipFile(Paths.get(""));
//		teks = dt.getDiagnosisKeysForDate(LocalDate.of(2020, 06, 23));
		assertEquals(503, teks.orElseThrow().getKeysCount());
//		teks.ifPresent(k -> System.out.format("found %d keys\n", k.getKeysCount()));
	}

	@Test
	public void readFromExtracted() {
		try (InputStream inputStream = Files.newInputStream(Paths.get(""))) {
			final ByteBuffer buffer = ByteBuffer.wrap(inputStream.readAllBytes());
			buffer.position(16);

			final TemporaryExposureKeyExport keyExport = TemporaryExposureKeyExport.parseFrom(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void transmissionRiskLevelStatistics() {
		DE_Distribution dt = new DE_Distribution();
		Optional<TemporaryExposureKeyExport> teks;
		teks = dt.extractKeysFromZipFile(Paths.get(""));
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
		final Map<Integer, Long> map = teks.orElseThrow().getKeysList().stream()
												.map(TemporaryExposureKey::getTransmissionRiskLevel)
												.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> System.out.printf("%d: %d\n", entry.getKey(), entry.getValue()));
	}



	@Test
	public void miscStatistics() {
		DE_Distribution dt = new DE_Distribution();
		TemporaryExposureKeyExport teks;
		teks = dt.extractKeysFromZipFile(Paths.get("")).orElseThrow();

		System.out.println("teks.getStartTimestamp() = " + teks.getStartTimestamp());
		System.out.println("Instant.ofEpochSecond(teks.getStartTimestamp()) = " + Instant.ofEpochSecond(teks.getStartTimestamp()));
		System.out.println("Instant.ofEpochSecond(teks.getEndTimestamp()) = " + Instant.ofEpochSecond(teks.getEndTimestamp()));

	}

	@Test
	public void rollingStartIntervalNumberStatistics() {
		LocalTime midnight = LocalTime.of(0,0);
		final Map<Instant, Long> map = getDefaultKeys().stream()
														   .map(TemporaryExposureKey::getRollingStartIntervalNumber)
														   .map(ENIntervalNumberUtils::getUnixTimeInstant)

														   .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((entry) -> System.out.printf("%s: %d\n", entry.getKey(), entry.getValue()));

		getDefaultKeys().stream()
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