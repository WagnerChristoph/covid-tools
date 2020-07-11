import distribution.de.DE_Distribution;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport;
import util.IOUtils;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class KeyDownloader {

	public static final String KEY_DIR_PATH = "keys";

	private static final Logger logger = LogManager.getLogger(KeyDownloader.class);

	private final IOUtils ioUtils;
	private final DE_Distribution DEDistribution;


	public KeyDownloader(String keyDir) {
		this.ioUtils = new IOUtils(Paths.get(Objects.requireNonNull(keyDir)));
		this.DEDistribution = new DE_Distribution();
	}

	public KeyDownloader() {
		this(KEY_DIR_PATH);
	}


	public void downloadKeys() {
		final Set<LocalDate> existingDates = ioUtils.getExistingDates().keySet();
		logger.info("found {} existing files", existingDates.size());

		final Set<LocalDate> availableDates = new HashSet<>(DEDistribution.getAvailableDays());
		logger.info("found {} available dates on server", availableDates.size());
		availableDates.removeAll(existingDates);
		logger.info("requesting {} new files", availableDates.size());
//		final List<TemporaryExposureKeyExport> newTEKs = availableDates.stream()
//																	   .map(distribution::getDiagnosisKeysForDate)
//																	   .flatMap(Optional::stream)
//																	   .collect(Collectors.toList());


		final Map<LocalDate, TemporaryExposureKeyExport> newTEKs = availableDates.stream()
																													  .map(DEDistribution::getDiagnosisKeysForDayWithDay)
																													  .flatMap(Optional::stream)
																													  .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		logger.info("received {} new files", newTEKs.size());


		final int newFilecount = ioUtils.persistTEKs(newTEKs);
		logger.info("wrote {} new files", newFilecount);
//		Map<LocalDate, TemporaryExposureKeyExport> newTEKs;
//		for (LocalDate date : availableDates) {
//			final var tek = distribution.getDiagnosisKeysForDate(date)
//					.map;
//		}

	}


	public static void main(String[] args) {
		//todo config path
		KeyDownloader kd = new KeyDownloader("");
		kd.downloadKeys();
	}

}
