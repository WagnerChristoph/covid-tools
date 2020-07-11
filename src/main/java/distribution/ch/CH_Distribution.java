package distribution.ch;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import distribution.AbstractDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Optional;

public class CH_Distribution extends AbstractDistribution {
	public static final String BASE_URL = "https://www.pt.bfs.admin.ch/v1/gaen/exposed";
	public static final int MS_IN_SECONDS = 1000;

	private static final Logger logger = LogManager.getLogger(CH_Distribution.class);

	@Override
	protected String baseURL() {
		return BASE_URL;
	}

	@Override
	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date) {
		final long epochMillis = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * MS_IN_SECONDS;
		String url = String.format("%s/%d", BASE_URL, epochMillis);
		logger.info("requesting diagnosis keys for {}", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return getKeyFile(url);
	}


	public static void main(String[] args) {
		//test
		final LocalDate date = LocalDate.of(2020, 6, 26);
		CH_Distribution dt = new CH_Distribution();
		final var diagnosisKeys = dt.getDiagnosisKeysForDay(date).orElseThrow();
		System.out.println(diagnosisKeys.getKeysCount());
	}



}
