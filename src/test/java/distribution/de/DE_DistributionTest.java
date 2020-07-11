package distribution.de;

import com.google.gson.JsonArray;
import distribution.AbstractDistributionTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protobuf.TemporaryExposureKeyExport;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DE_DistributionTest {


	public static final LocalDate EXAMPLE_DATE = LocalDate.of(2020, 9, 8);
	public static final LocalDateTime EXAMPLE_DATE_TIME = LocalDateTime.of(2020, 9, 8, 4, 0);

	private  MockWebServer mockServer;
	private  DE_Distribution dist;


	@BeforeEach
	public void setUp() throws IOException{
		mockServer = new MockWebServer();
		mockServer.start();
		String baseUrl = mockServer.url("").toString();
		dist = new DE_Distribution(baseUrl);
	}

	@Test
	void getAvailableDays() throws InterruptedException{
		List<LocalDate> days = List.of(EXAMPLE_DATE,
				EXAMPLE_DATE.plusDays(1),
				EXAMPLE_DATE.plusDays(2),
				EXAMPLE_DATE.plusDays(3));
		final JsonArray jsonDateArr = new JsonArray();
		days.stream()
			 .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
			 .forEach(jsonDateArr::add);
		mockServer.enqueue(new MockResponse().setBody(jsonDateArr.toString()));

		final List<LocalDate> actual = dist.getAvailableDays();

		assertEquals(days, actual);
		final RecordedRequest request = mockServer.takeRequest();
		assertEquals("/version/v1/diagnosis-keys/country/DE/date", request.getPath());
		}


	@Test
	void getAvailableDays_Empty() {
		mockServer.enqueue(new MockResponse().setBody(new JsonArray().toString()));
		final List<LocalDate> actual = dist.getAvailableDays();

		assertTrue(actual.isEmpty());
	}

	@Test
	void getAvailableDays_NotFound() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
		final List<LocalDate> actual = dist.getAvailableDays();

		assertTrue(actual.isEmpty());
	}


	@Test
	void getAvailableHours() throws InterruptedException {
		List<Integer> hours = List.of(1, 10, 15, 23);
		final JsonArray jsonDateArr = new JsonArray();
		hours.forEach(jsonDateArr::add);
		mockServer.enqueue(new MockResponse().setBody(jsonDateArr.toString()));

		final List<LocalDateTime> actual = dist.getAvailableHours(EXAMPLE_DATE);

		assertEquals(4, actual.size());
		assertEquals("/version/v1/diagnosis-keys/country/DE/date/2020-09-08/hour", mockServer.takeRequest().getPath());
	}

	@Test
	void getAvailableHours_Empty() {
		mockServer.enqueue(new MockResponse().setBody(new JsonArray().toString()));
		final List<LocalDateTime> actual = dist.getAvailableHours(EXAMPLE_DATE);

		assertTrue(actual.isEmpty());
	}

	@Test
	void getAvailableHours_NotFound() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
		final List<LocalDateTime> actual = dist.getAvailableHours(EXAMPLE_DATE);

		assertTrue(actual.isEmpty());
	}

	@Test
	void getDiagnosisKeyForDay() throws IOException, InterruptedException {
		mockServer.enqueue(AbstractDistributionTest.getDiagnosisKeysTestFileMockResponse());
		final TemporaryExposureKeyExport tekExport = dist.getDiagnosisKeysForDay(EXAMPLE_DATE).orElseThrow();

		assertEquals(2, tekExport.getKeysCount());
		assertEquals("/version/v1/diagnosis-keys/country/DE/date/2020-09-08", mockServer.takeRequest().getPath());
	}

	@Test
	void getDiagnosisKeyForDay_NotFound() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
		final Optional<TemporaryExposureKeyExport> optional = dist.getDiagnosisKeysForDay(EXAMPLE_DATE);

		assertTrue(optional.isEmpty());
	}

	@Test
	void getDiagnosisKeyForHour() throws IOException, InterruptedException {
		mockServer.enqueue(AbstractDistributionTest.getDiagnosisKeysTestFileMockResponse());
		final TemporaryExposureKeyExport tekExport = dist.getDiagnosisKeysForHour(EXAMPLE_DATE_TIME).orElseThrow();

		assertEquals(2, tekExport.getKeysCount());
		assertEquals("/version/v1/diagnosis-keys/country/DE/date/2020-09-08/hour/4", mockServer.takeRequest().getPath());
	}

	@Test
	void getDiagnosisKeyForHour_NotFound() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
		final Optional<TemporaryExposureKeyExport> optional = dist.getDiagnosisKeysForHour(EXAMPLE_DATE_TIME);

		assertTrue(optional.isEmpty());
	}

	@AfterEach
	void tearDown() throws IOException{
		mockServer.shutdown();
	}
}