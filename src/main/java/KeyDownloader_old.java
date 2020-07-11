import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KeyDownloader_old {

	public static final String KEYS_FILE_NAME = "keys";
	public static final String DATES_FILE_NAME = "dates";

	private static final Logger logger = LogManager.getLogger(KeyDownloader_old.class);


	private final IOFactory ioFactory;
	private final Path keysFile;
	private final Path datesFile;


	public KeyDownloader_old() {
		this(DATES_FILE_NAME, KEYS_FILE_NAME);
	}

	public KeyDownloader_old(String datesFile, String keysFile) {
		this.datesFile = Paths.get(Objects.requireNonNull(datesFile));
		this.keysFile = Paths.get(Objects.requireNonNull(keysFile));
		this.ioFactory = new IOFactory(this);
	}

	 KeyDownloader_old(IOFactory ioFactory, String datesFile, String keysFile) {
		this.ioFactory = ioFactory;
		this.datesFile = Paths.get(Objects.requireNonNull(datesFile));
		this.keysFile = Paths.get(Objects.requireNonNull(keysFile));
	}





	public static void main(String[] args) {
		logger.info("created Downloader");
		final ByteString hello_world = ByteString.copyFromUtf8("hello world");
		System.out.println(hello_world.toStringUtf8());


	}


	private void doWork() {
		if (Files.exists(keysFile)) {
//			loadDatesFromKeysFile(keysFile);
		} else{
			logger.info("creating new keys file");
			try {
				//todo adjust testing for Files.exists check
				createKeysFile(keysFile);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private void persistOnDisk() {

	}

	private Set<LocalDate> loadDatesFromKeysFile() throws IOException{

		return StreamSupport.stream(JsonParser.parseString(ioFactory.readAll(keysFile)).getAsJsonObject()
								.get("dates")
								.getAsJsonArray()
								.spliterator(), true)
					 .map(JsonElement::toString)
					 .map(s -> LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE))
					 .collect(Collectors.toSet());
	}


	private String formatDates(Set<LocalDate> dates) {
		final JsonObject datesJsonOb = new JsonObject();
		final JsonArray datesArr = new JsonArray();
		for (LocalDate date : dates) {
			datesArr.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		}
		datesJsonOb.add("dates", datesArr);
		return datesJsonOb.toString();
	}


	 void createKeysFile(Path file) throws IOException {
		ioFactory.create(file).write(formatDates(Set.of()) + '\n');
	}


//	private static JsonObject TEKExportToJson(TemporaryExposureKeyExport tekExport, LocalDate day) {
//		JsonObject jsonObj = new JsonObject();
//		jsonObj.addProperty("day", day.format(DateTimeFormatter.ISO_LOCAL_DATE));
//		jsonObj.addProperty("start_date", LocalDateTime.ofEpochSecond(tekExport.getStartTimestamp(), 0, ZoneOffset.UTC)
//													   .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//		jsonObj.addProperty("end_date", LocalDateTime.ofEpochSecond(tekExport.getEndTimestamp(), 0, ZoneOffset.UTC)
//													   .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//		jsonObj.addProperty("region", tekExport.getRegion());
//		jsonObj.addProperty("num_keys", tekExport.getKeysCount());
//		JsonArray keys = new JsonArray();
//		for (TemporaryExposureKey tek : tekExport.getKeysList()) {
//			keys.add(TEKtoJson(tek));
//		}
//		return jsonObj;
//	}
//
//
//	private static JsonObject TEKtoJson(TemporaryExposureKey tek) {
//		JsonObject jsonObj = new JsonObject();
//		jsonObj.addProperty("key_data", new String(Hex.encodeHex(tek.getKeyData().asReadOnlyByteBuffer())));
//		jsonObj.addProperty("transmission_risk_level", tek.getTransmissionRiskLevel());
//		jsonObj.addProperty("rolling_start_interval_number", tek.getRollingStartIntervalNumber());
//		return jsonObj;
//	}

	public static class IOFactory {

		private final KeyDownloader_old kd;

		public IOFactory(KeyDownloader_old kd) {
			this.kd = kd;
		}

		public Writer create(Path p) throws IOException {
			return Files.newBufferedWriter(p, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		}
		public Writer append(Path p) throws IOException {
			return Files.newBufferedWriter(p, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
		}

		public Reader read(Path p) throws IOException {
			return Files.newBufferedReader(p);
		}
		public String readAll(Path p) throws IOException {
			return Files.readString(p);
		}
	}





}
