package distribution.ch;

import distribution.AbstractDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static protobuf.TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport;

public class CH_Distribution extends AbstractDistribution {
	public static final String BASE_URL = "https://www.pt.bfs.admin.ch/v1/gaen/exposed";
	public static final int MS_IN_SECONDS = 1000;

	private static final Logger logger = LogManager.getLogger(CH_Distribution.class);


	@Override
	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date) {
		final long epochMillis = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * MS_IN_SECONDS;
		String url = String.format("%s/%d", BASE_URL, epochMillis);
		logger.info("requesting diagnosis keys for {}", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return getKeyFile(url);
	}

}
