package util;

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

import static protobuf.TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport;

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

}
