package download.at;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import download.AbstractDistribution;
import download.Callback;
import download.DaysIndexableDistribution;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.TemporaryExposureKeyExport;
import util.ENIntervalNumberUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

public class AT_Distribution extends AbstractDistribution implements DaysIndexableDistribution {
	public static final String BASE_URL = "https://cdn.prod-rca-coronaapp-fd.net/";
	public static final String INDEX_PATH = "exposures/at/index.json";

	private static final Logger logger = LogManager.getLogger(AT_Distribution.class);
	private static final LocalTime startOfDay = LocalTime.of(0, 0);

	private  Map<LocalDate, String> fileIndex;
	private LocalDateTime indexLastRequest;
	private TemporalAmount indexCacheExpirationDuration = Duration.ofMinutes(10);


	public AT_Distribution(String baseUrl) {
		super(baseUrl);
	}

	public AT_Distribution() {
		this(BASE_URL);
	}

	public void setIndexCacheExpirationDuration(int seconds) {
		if(seconds <= 0) {
			throw new IllegalArgumentException("expiration must be > 0");
		}
		indexCacheExpirationDuration = Duration.ofSeconds(seconds);
	}

	//lazy, cached index
	private Map<LocalDate, String> getFileIndex() {
		if (fileIndex == null) {
			logger.debug("requesting file index initially");
			fileIndex = parseAvailableIndex();
			indexLastRequest = LocalDateTime.now();
		} else if (indexLastRequest != null) {
			if (indexLastRequest.isBefore(LocalDateTime.now().minus(indexCacheExpirationDuration))) {
				logger.debug("index is expired, requesting new");
				fileIndex = parseAvailableIndex();
				indexLastRequest = LocalDateTime.now();
			}
			logger.debug("found existing valid file index");
		} else {
			logger.error("index last request time is null, requesting new");
			fileIndex = parseAvailableIndex();
			indexLastRequest = LocalDateTime.now();
		}
		return fileIndex;
	}


	/**
	 * Request and parse the index.
	 * @return Map containing all available daily temporary exposure key file dates with corresponding url.
	 */
	private Map<LocalDate, String> parseAvailableIndex() {
		return Optional.ofNullable(getAvailableFilesIndex().get("daily_batches"))
					   .map(JsonElement::getAsJsonArray)
					   .filter(arr -> arr.size() != 0)
					   .stream()
					   .flatMap(Streams::stream)
					   .map(JsonElement::getAsJsonObject)
					   .map(this::parseIndexEntry)
					   .flatMap(Optional::stream)
					   .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	//actually request the index
	private Map<String, JsonElement> getAvailableFilesIndex() {
		logger.info("requesting available files index");
		String indexUrl = String.format("%s%s", baseUrl, INDEX_PATH);
		Map<String, JsonElement> entries = new HashMap<>();
		Callback<Response> callback = new Callback<>() {
			@Override
			public void onSuccess(Response item) {
				try (ResponseBody body = item.body()) {
					final JsonObject jsonRootObj = JsonParser.parseString(body.string()).getAsJsonObject();
					//return all entries in index
					jsonRootObj.entrySet().forEach(entry -> entries.put(entry.getKey(), entry.getValue()));
//							   .map(entry -> entry.getValue().getAsJsonObject())
//							   .forEach(items::add);

//					Optional.ofNullable(jsonRootObj)
//							.map(obj -> obj.getAsJsonArray("daily_batches"))
//							.stream()
//							.flatMap(Streams::stream)
//							.map(JsonElement::getAsJsonObject)
//							.map(obj -> ENIntervalNumberUtils.getUnixTimeInstant(obj.get("interval").getAsInt()))
//							.forEach(System.out::println);

				}catch (IOException e) {
					logger.error("error in response: {}", e.getMessage());
				}
			}

			@Override
			public void onError(Throwable t) {
				logger.error("error requesting index: {}", t.getMessage());
			}
		};
		executeRequest(indexUrl, callback);
		return entries;
	}



	@Override
	public Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date) {
		return Optional.ofNullable(getFileIndex().get(date))
				.flatMap(this::getKeyFile);
	}

	@Override
	public List<TemporaryExposureKeyExport> getAllAvailableKeys() {
		logger.debug("requesting available days");
		return getFileIndex().values().stream()
							 .map(this::getKeyFile)
							 .flatMap(Optional::stream)
							 .collect(Collectors.toList());
	}

	@Override
	public List<TemporaryExposureKeyExport> requestAll(Collection<LocalDate> dates) {
		final Map<LocalDate, String> dateMap = getFileIndex();
		return dates.stream()
			 .map(date -> Optional.ofNullable(dateMap.get(date)))
			 .flatMap(Optional::stream)
			.map(this::getKeyFile)
			 .flatMap(Optional::stream)
			 .collect(Collectors.toList());

	}


	@Override
	public List<LocalDate> getAvailableDays() {
		logger.debug("requesting available days");
		return new ArrayList<>(getFileIndex().keySet());
	}


	/**
	 * Downloads the 'full_14_batch' file, if available in the index.
	 * @return If successful, the temporary exposure key export file.
	 */
	public Optional<TemporaryExposureKeyExport> getFull14Batch() {
		logger.debug("requesting full_14_batch");
		return Optional.ofNullable(getAvailableFilesIndex().get("full_14_batch"))
					   .map(JsonElement::getAsJsonObject)
//					   .flatMap(this::getDownloadPathFromIndexEntry)
					   .flatMap(this::parseIndexEntry)
					   .map(Pair::getValue)
					   .flatMap(this::getKeyFile);
	}

	/**
	 * Downloads the 'full_7_batch' file, if available in the index.
	 * @return If successful, the temporary exposure key export file.
	 */
	public Optional<TemporaryExposureKeyExport> getFull7Batch() {
		logger.debug("requesting full_7_batch");
		return Optional.ofNullable(getAvailableFilesIndex().get("full_7_batch"))
					   .map(JsonElement::getAsJsonObject)
//					   .flatMap(this::getDownloadPathFromIndexEntry)
					   .flatMap(this::parseIndexEntry)
					   .map(Pair::getValue)
					   .flatMap(this::getKeyFile);
	}

//	private Optional<String> getDownloadPathFromIndexEntry(JsonObject entry) {
//		return Optional.ofNullable(entry.get("batch_file_paths"))
//					   .map(JsonElement::getAsJsonArray)
//					   .filter(arr -> arr.size() != 0)
//					   .map(arr -> arr.get(0).getAsString())
//					   //remove leading '/'
//					   .map(s -> baseUrl + s.substring(1));
//	}

	/**
	 * Parses an entry in the index file to get the date and corresponding url.
	 * @param entry The entry to parse.
	 * @return If successful, the date and url contained in the entry.
	 */
	private Optional<Pair<LocalDate, String>> parseIndexEntry(JsonObject entry) {
		JsonArray paths;
		if(entry.has("interval") && entry.has("batch_file_paths")) {
			if((paths = entry.get("batch_file_paths").getAsJsonArray()).size() != 0) {
				//remove leading '/'
				String path = baseUrl + paths.get(0).getAsString().substring(1);
				final LocalDateTime interval = ENIntervalNumberUtils.getLocalDate(entry.get("interval").getAsInt());
				if(!interval.toLocalTime().equals(startOfDay)) {
					logger.warn("interval is not at start of day");
				}

				return Optional.of(ImmutablePair.of(interval.toLocalDate(), path));
			}
		}
		return Optional.empty();
	}
	}
