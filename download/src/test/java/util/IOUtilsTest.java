package util;


import model.TEKExport;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import protobuf.TemporaryExposureKeyExport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IOUtilsTest {

	//todo: test (de-)serialization

	@Test
	void testPersistTEKs(@TempDir Path keyDir) throws IOException {
		IOUtils ioUtils = new IOUtils(keyDir);
		int numDays = 10;
		LocalDate start = LocalDate.now().minusDays(numDays);
		final Map<LocalDate, TemporaryExposureKeyExport> map = start.datesUntil(LocalDate.now())
																		.map(d -> ImmutablePair.of(d, KeysUtils.genTemporaryExposureKeyExport(5, 0)))
																		.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		final int count = ioUtils.persistTEKs(map);

		assertEquals(numDays,count);
		assertEquals(numDays, Files.list(keyDir).count());
		//todo check file names
	}

	@Test
	void testDeserialize(@TempDir Path keyDir) throws IOException {
		IOUtils ioUtils = new IOUtils(keyDir);
		LocalDate d = LocalDate.now();
		final Map<LocalDate, TemporaryExposureKeyExport> map = Map.of(d, KeysUtils.genTemporaryExposureKeyExport(5, 0));
		ioUtils.persistTEKs(map);

		final TEKExport tekExport = ioUtils.deserialize(Files.list(keyDir).findFirst().orElseThrow());
		assertEquals(5, tekExport.getKeys().size());
		assertTrue(tekExport.getRevisedKeys().isEmpty());
		// ...
	}

}