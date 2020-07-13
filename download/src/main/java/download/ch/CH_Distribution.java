package download.ch;

import download.AbstractDistribution;
import download.Distribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.TemporaryExposureKeyExport;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Queries the Swiss Corona CDN
 */
public class CH_Distribution extends AbstractDistribution implements Distribution {
	public static final String BASE_URL = "https://www.pt.bfs.admin.ch/";
	public static final String DEFAULT_VERSION = "v1";
	public static final int MS_IN_SECONDS = 1000;

	private static final Logger logger = LogManager.getLogger(CH_Distribution.class);


	public CH_Distribution() {
		this(BASE_URL);
	}

	public CH_Distribution(String baseUrl) {
		super(baseUrl);
	}

	private String buildDefaultBaseUrl() {
		return buildBaseUrl(DEFAULT_VERSION);
	}

	private String buildBaseUrl(String version) {
		return String.format("%s%s/gaen/exposed", this.baseUrl, version);
	}


	@Override
	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date) {
		final long epochMillis = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * MS_IN_SECONDS;
		String url = String.format("%s/%d", buildDefaultBaseUrl(), epochMillis);
		logger.info("requesting diagnosis keys for {}", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return getKeyFile(url);
	}

}
