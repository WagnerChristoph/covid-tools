package download.de;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import download.AbstractDistribution;
import download.Callback;
import download.DaysIndexableDistribution;
import download.HoursIndexableDistribution;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.TemporaryExposureKeyExport;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class DE_Distribution extends AbstractDistribution implements DaysIndexableDistribution, HoursIndexableDistribution {
	public static final String DEFAULT_COUNTRY = "DE";
	public static final String DEFAULT_VERSION = "v1";
	public static final String BASE_URL = "https://svc90.main.px.t-online.de/";

	private static final Logger logger = LogManager.getLogger(DE_Distribution.class);

	private final String baseUrl;

	public DE_Distribution() {
		this(BASE_URL);
	}

	public DE_Distribution(String baseUrl) {
		this.baseUrl = baseUrl;
	}


	private String getDefaultAvailableDaysUrl() {
		return getAvailableDaysUrl(DEFAULT_VERSION, DEFAULT_COUNTRY);
	}

	private String getAvailableDaysUrl(String version, String country) {
		return String.format("%sversion/%s/diagnosis-keys/country/%s/date", this.baseUrl, version, country);
	}


	@Override
	public List<LocalDate> getAvailableDays() {
		return getAvailableDays(DEFAULT_COUNTRY);
	}

	public List<LocalDate> getAvailableDays(String country) {
		final String url = getAvailableDaysUrl(DEFAULT_VERSION, country);
		logger.debug("requesting available days");
		return getAvailableDates(url).stream()
									 .map(dateString -> LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE))
									 .collect(Collectors.toList());
	}

	@Override
	public List<LocalDateTime> getAvailableHours(LocalDate day) {
		return getAvailableHours(day, DEFAULT_COUNTRY);
	}

	public List<LocalDateTime> getAvailableHours(LocalDate day, String country) {
		final String url = String.format("%s/%s/hour", getAvailableDaysUrl(DEFAULT_VERSION, country), day.format(DateTimeFormatter.ISO_LOCAL_DATE));
		logger.debug("requesting available hours for {}", day.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return getAvailableDates(url).stream()
									 .mapToInt(Integer::parseInt)
									 .mapToObj(i -> day.atTime(i, 0))
									 .collect(Collectors.toList());
	}


	private List<String> getAvailableDates(String url) {
		List<String> list = new ArrayList<>();
		logger.debug("requesting dates with: {}", url);
		Callback<Response> callback = new Callback<>() {
			@Override
			public void onSuccess(Response item) {
				try(ResponseBody body = item.body()) {
					list.addAll(Streams.stream(JsonParser.parseString(body.string())
													.getAsJsonArray()
													.iterator())
								  .map(JsonElement::getAsString)
								  .collect(Collectors.toList()));
				}catch (IOException e) {
					logger.error("error in response: {}", e.getMessage());
				}

			}

			@Override
			public void onError(Throwable t) {
				logger.error("error requesting dates: {}", t.getMessage());
			}
		};

		executeRequest(url, callback);
		return list;
	}


	@Override
	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date) {
		return getDiagnosisKeysForDay(date, DEFAULT_COUNTRY);
	}

	private Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date, String country) {
//		List<TemporaryExposureKeyExport> keys = new ArrayList<>();
		final String url = String.format("%s/%s", getAvailableDaysUrl(DEFAULT_VERSION, country), date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		logger.info("requesting diagnosis keys for {}", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return getKeyFile(url);
	}

	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForHour(LocalDateTime dateTime) {
		return getDiagnosisKeysForHour(dateTime, DEFAULT_COUNTRY);
	}

	private Optional<TemporaryExposureKeyExport> getDiagnosisKeysForHour(LocalDateTime dateTime, String country) {
//		List<TemporaryExposureKeyExport> keys = new ArrayList<>();
		final String url = String.format("%s/%s/hour/%d", getAvailableDaysUrl(DEFAULT_VERSION, country), dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), dateTime.get(ChronoField.HOUR_OF_DAY));
		logger.info("requesting diagnosis keys for {}, hour {}", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), dateTime.get(ChronoField.HOUR_OF_DAY));
		return getKeyFile(url);
	}


}
