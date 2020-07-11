package util;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.TEKExport;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IOUtils {
	public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	private static final Logger logger = LogManager.getLogger(IOUtils.class);

	private final Path keyDir;
	private final Gson gson;


	public IOUtils(Path keyDir) {
		this.gson = new GsonBuilder()
				.registerTypeAdapter(LocalDate.class, new TEKExport.LocalDateAdapter().nullSafe())
				.registerTypeAdapter(LocalDateTime.class, new TEKExport.LocalDateTimeAdapter().nullSafe())
				.create();

		this.keyDir = keyDir;
		if (Files.notExists(keyDir)) {
			try {
				Files.createDirectory(keyDir);
			} catch (IOException e) {
				logger.error("could not create key directory: {}", e.getMessage());
				throw new IllegalArgumentException(e);
			}
		}
	}

	private static String formatFilename(LocalDate date) {
		return String.format("%s.json", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

//		static JsonObject TEKExportToJson(TemporaryExposureKeyExport tekExport, LocalDate day) {
//			JsonObject jsonObj = new JsonObject();
//			jsonObj.addProperty("day", day.format(DateTimeFormatter.ISO_LOCAL_DATE));
//			jsonObj.addProperty("start_date", LocalDateTime.ofEpochSecond(tekExport.getStartTimestamp(), 0, ZoneOffset.UTC)
//														   .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//			jsonObj.addProperty("end_date", LocalDateTime.ofEpochSecond(tekExport.getEndTimestamp(), 0, ZoneOffset.UTC)
//														 .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//			jsonObj.addProperty("region", tekExport.getRegion());
//			jsonObj.addProperty("num_keys", tekExport.getKeysCount());
//			JsonArray keys = new JsonArray();
//			for (TemporaryExposureKey tek : tekExport.getKeysList()) {
//				keys.add(TEKtoJson(tek));
//			}
//			jsonObj.add("keys", keys);
//			return jsonObj;
//		}
//
//		static JsonObject TEKtoJson(TemporaryExposureKey tek) {
//			JsonObject jsonObj = new JsonObject();
//			jsonObj.addProperty("key_data", new String(Hex.encodeHex(tek.getKeyData().asReadOnlyByteBuffer())));
//			jsonObj.addProperty("transmission_risk_level", tek.getTransmissionRiskLevel());
//			jsonObj.addProperty("rolling_start_interval_number", tek.getRollingStartIntervalNumber());
//			jsonObj.addProperty("day", LocalDateTime.ofInstant(util.ENIntervalNumberUtils.getUnixTimeInstant(tek.getRollingStartIntervalNumber()), UTC).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
//			return jsonObj;
//		}


	public int persistTEKs(Map<LocalDate, TemporaryExposureKeyExport> teks) {
		int successCount = 0;
		for (Map.Entry<LocalDate, TemporaryExposureKeyExport> entry : teks.entrySet()) {
			String filename = formatFilename(entry.getKey());
			try {
				Files.writeString(keyDir.resolve(filename),
						gson.toJson(TEKExport.fromProtobuf(entry.getValue(), entry.getKey())),
//							TEKExportToJson(entry.getValue(), entry.getKey()).toString(),
						StandardOpenOption.CREATE_NEW);
				successCount++;
			} catch (IOException e) {
				logger.error("error writing file: {}: {}", filename, e.getMessage());
			}
		}
		return successCount;
	}


//		 Set<LocalDate> getExistingDates() {
//			Set<LocalDate> files = Collections.emptySet();
//			try (Stream<Path> list = Files.list(keyDir)) {
//				files = list.filter(p -> FilenameUtils.getExtension(p.getFileName().toString()).equals("json"))
//							.map(p -> {
//								final String fileName = FilenameUtils.removeExtension(p.getFileName().toString());
//								LocalDate fileDate = null;
//								try {
//									fileDate = LocalDate.parse(fileName, DEFAULT_DATE_TIME_FORMATTER);
//								} catch (DateTimeParseException ignored) {
//								}
//								return Optional.ofNullable(fileDate);
//							})
//							.flatMap(Optional::stream)
//							.collect(Collectors.toSet());
//							logger.debug("found existing dates: {}", files.stream().map(DEFAULT_DATE_TIME_FORMATTER::format).collect(Collectors.joining()));
//			} catch (IOException e) {
//				logger.error("error reading files: {}", e.getMessage());
//			}
//			return files;
//		}

	public Optional<TEKExport> safelyDeserialize(Path p) {

		try {
			return Optional.ofNullable(deserialize(p));
		} catch (IOException e) {
			logger.error("error deserialzing file {}: {}", p.toString(), e.getMessage());
			return Optional.empty();
		}
	}

	public TEKExport deserialize(Path p) throws IOException {
		TEKExport deserialized;
		try (BufferedReader reader = Files.newBufferedReader(p)) {
			deserialized = gson.fromJson(reader, TEKExport.class);
		}
		return  deserialized;
	}



	public Set<Path> getExistingDateFiles() {
		return new HashSet<>(getExistingDates().values());
	}

	public Map<LocalDate, Path> getExistingDates() {
		Map<LocalDate, Path> filesMap = Collections.emptyMap();
		try (Stream<Path> list = Files.list(keyDir)) {
			filesMap = list.filter(p -> FilenameUtils.getExtension(p.getFileName().toString()).equals("json"))
						   .map(p -> {
							   final String fileName = FilenameUtils.removeExtension(p.getFileName().toString());
							   Pair<LocalDate, Path> result = null;
							   try {
								   LocalDate fileDate = LocalDate.parse(fileName, DEFAULT_DATE_TIME_FORMATTER);
								   result = ImmutablePair.of(fileDate, p);
							   } catch (DateTimeParseException ignored) {
							   }
							   return Optional.ofNullable(result);
						   })
						   .flatMap(Optional::stream)
						   .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
			logger.debug("found existing dates: {}", filesMap.keySet().stream().map(DEFAULT_DATE_TIME_FORMATTER::format).collect(Collectors.joining(",")));
		} catch (IOException e) {
			logger.error("error reading files: {}", e.getMessage());
		}
		return filesMap;
	}

	//todo
	public Optional<Path> getSavedFileForDate(LocalDate date) {
		return null;
	}

//	 List<Path> getExistingFiles(Path keyDir) throws IOException {
//			List<Path> files = new ArrayList<>();
//			try (Stream<Path> list = Files.list(keyDir)) {
//				files = list.filter(p -> FilenameUtils.getExtension(p.getFileName().toString()).equals("json"))
//							.filter(p -> {
//								final String fileName = FilenameUtils.removeExtension(p.getFileName().toString());
//								try {
//									DateTimeFormatter.ISO_LOCAL_DATE.parse(fileName);
//								} catch (DateTimeParseException e) {
//									return false;
//								}
//								return true;
//							})
//							.collect(Collectors.toList());
//			}
//			return files;
//		}

}
