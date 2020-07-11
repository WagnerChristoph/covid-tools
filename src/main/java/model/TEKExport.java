package model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;
import protobuf.TemporaryExposureKey;
import protobuf.TemporaryExposureKeyExport;
import util.ENIntervalNumberUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class TEKExport {
	public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = ISO_LOCAL_DATE;
	public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	public static class LocalDateAdapter extends TypeAdapter<LocalDate> {
		@Override
		public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
			jsonWriter.value(localDate.format(DEFAULT_DATE_FORMATTER));
		}

		@Override
		public LocalDate read(JsonReader jsonReader) throws IOException {
			return LocalDate.parse(jsonReader.nextString(), DEFAULT_DATE_FORMATTER);
		}
	}

	public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
		@Override
		public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
			jsonWriter.value(localDateTime.format(DEFAULT_DATE_TIME_FORMATTER));
		}

		@Override
		public LocalDateTime read(JsonReader jsonReader) throws IOException {
			return LocalDateTime.parse(jsonReader.nextString(), DEFAULT_DATE_TIME_FORMATTER);
		}
	}

	@Nullable
	@SerializedName("day")
	private final LocalDate day;

	@Nullable
	@SerializedName("start_date")
	private final LocalDateTime startDate;

	@Nullable
	@SerializedName("end_date")
	private final LocalDateTime endDate;

	@SerializedName("region")
	private final String region;

	@SerializedName("num_keys")
	private final int num_keys;

	@SerializedName("keys")
	private final List<TEK> keys;

	public TEKExport(@Nullable LocalDate day, @Nullable LocalDateTime startDate, @Nullable LocalDateTime endDate, String region, int num_keys, List<TEK> keys) {
		this.day = day;
		this.startDate = startDate;
		this.endDate = endDate;
		this.region = Objects.requireNonNullElse(region, "");
		this.num_keys = num_keys;
		this.keys = new LinkedList<>(keys);
	}

	@Nullable
	public LocalDate getDay() {
		return day;
	}

	@Nullable
	public LocalDateTime getStartDate() {
		return startDate;
	}

	@Nullable
	public LocalDateTime getEndDate() {
		return endDate;
	}

	public String getRegion() {
		return region;
	}

	public int getNum_keys() {
		return num_keys;
	}

	public List<TEK> getKeys() {
		return new LinkedList<>(keys);
	}

	public static TEKExport fromProtobuf(TemporaryExposureKeyExport tekExport, @Nullable LocalDate day){
		return new TEKExport(
				day,
				tekExport.hasStartTimestamp() ? LocalDateTime.ofEpochSecond(tekExport.getStartTimestamp(), 0, ZoneOffset.UTC) : null,
				tekExport.hasEndTimestamp() ? LocalDateTime.ofEpochSecond(tekExport.getEndTimestamp(), 0, ZoneOffset.UTC) : null,
				tekExport.hasRegion() ? tekExport.getRegion() : "",
				tekExport.getKeysCount(),
				tekExport.getKeysList().stream()
						 .map(TEK::fromProtobuf)
						 .collect(Collectors.toList()));
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TEKExport tekExport = (TEKExport) o;
		return num_keys == tekExport.num_keys &&
				Objects.equals(day, tekExport.day) &&
				Objects.equals(startDate, tekExport.startDate) &&
				Objects.equals(endDate, tekExport.endDate) &&
				region.equals(tekExport.region) &&
				keys.equals(tekExport.keys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(day, startDate, endDate, region, num_keys, keys);
	}

	public static class TEK {

		@SerializedName("key_data")
		private final String keyData;

		@SerializedName("transmission_risk_level")
		private final int transmissionRiskLevel;

		@SerializedName("rolling_start_interval_number")
		private final int rollingStartIntervalNumber;

		@Nullable
		@SerializedName("day")
		private final LocalDate day;



		public TEK(String keyData, int transmissionRiskLevel, int rollingStartIntervalNumber, @Nullable LocalDate day) {
			this.keyData = Objects.requireNonNullElse(keyData, "");
			this.transmissionRiskLevel = transmissionRiskLevel;
			this.rollingStartIntervalNumber = rollingStartIntervalNumber;
			this.day = day;
		}

		public String getKeyData() {
			return keyData;
		}

		public int getTransmissionRiskLevel() {
			return transmissionRiskLevel;
		}

		public int getRollingStartIntervalNumber() {
			return rollingStartIntervalNumber;
		}

		@Nullable
		public LocalDate getDay() {
			return day;
		}


		//todo: deprecate transmission risk level
		public static TEK fromProtobuf(TemporaryExposureKey tek) {
			return new TEK(
					tek.hasKeyData() ? new String(Hex.encodeHex(tek.getKeyData().asReadOnlyByteBuffer())) : "",
					tek.hasTransmissionRiskLevel() ? tek.getTransmissionRiskLevel() : -1,
					tek.hasRollingStartIntervalNumber() ? tek.getRollingStartIntervalNumber() : -1,
					tek.hasRollingStartIntervalNumber() ?  LocalDate.ofInstant(ENIntervalNumberUtils.getUnixTimeInstant(tek.getRollingStartIntervalNumber()), UTC) : null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TEK tek = (TEK) o;
			return transmissionRiskLevel == tek.transmissionRiskLevel &&
					rollingStartIntervalNumber == tek.rollingStartIntervalNumber &&
					keyData.equals(tek.keyData) &&
					Objects.equals(day, tek.day);
		}

		@Override
		public int hashCode() {
			return Objects.hash(keyData, transmissionRiskLevel, rollingStartIntervalNumber, day);
		}
	}





}
