package download.at;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AT_DistributionTest {

	public static final int NUM_AVAILABLE_DAYS = 7;
	public static final Set<LocalDate> AVAILABLE_DATES = Set.of(LocalDate.of(2020, 7, 5),
																LocalDate.of(2020, 7, 6),
																LocalDate.of(2020, 7, 7),
																LocalDate.of(2020, 7, 8),
																LocalDate.of(2020, 7, 9),
																LocalDate.of(2020, 7, 10),
																LocalDate.of(2020, 7, 11));

	private MockWebServer mockServer;
	private AT_Distribution dist;

	@BeforeEach
	public void setUp() throws IOException {
		mockServer = new MockWebServer();
		mockServer.start();
		String baseUrl = mockServer.url("").toString();
		dist = new AT_Distribution(baseUrl);
	}

	private void enqueueIndex() {
		try {
			mockServer.enqueue(new MockResponse().setBody(new Buffer().write(Files.readAllBytes(Paths.get("src\\test\\files\\AT_index.json")))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void enqueue404Responses(int num) {
		for (int i = 0; i < num; i++) {
			mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
		}
	}


	@Test
	void testGetAvailableDays() throws InterruptedException {
		enqueueIndex();
		assertEquals(NUM_AVAILABLE_DAYS,(dist.getAvailableDays().size()));
		assertEquals("/exposures/at/index.json", mockServer.takeRequest().getPath());
		assertEquals(1, mockServer.getRequestCount());
	}


	@Test
	void testIndexFilePaths() throws InterruptedException {
		enqueueIndex();
		enqueue404Responses(NUM_AVAILABLE_DAYS);

		dist.getAllAvailableKeys();

		assertEquals(NUM_AVAILABLE_DAYS + 1, mockServer.getRequestCount());
		assertEquals("/exposures/at/index.json", mockServer.takeRequest().getPath());

		//get request paths
		Set<String> paths = new HashSet<>();
		for (int i = 0; i < NUM_AVAILABLE_DAYS; i++) {
			paths.add(mockServer.takeRequest().getPath());
		}
		//alt
//		final Set<String> paths = IntStream.iterate(0, i -> i < NUM_AVAILABLE_DAYS, i -> i++)
//											 .mapToObj(i -> {
//												 try {
//													 return mockServer.takeRequest().getPath();
//												 } catch (InterruptedException ignored) {
//													 return null;
//												 }
//											 })
//											 .collect(toSet());
//
		//NUM_AVAILABLE_DAYS different paths
		assertEquals(NUM_AVAILABLE_DAYS, paths.size());
	}

	@Test
	void testIndexCaching() {
		enqueueIndex();
		dist.getAvailableDays();
		dist.getAvailableDays();
		assertEquals(1, mockServer.getRequestCount());
	}

	@Test
	void testCacheExpiration() throws InterruptedException {
		enqueueIndex();
		enqueueIndex();
		dist.setIndexCacheExpirationDuration(1);
		dist.getAvailableDays();
		Thread.sleep(2000);
		dist.getAvailableDays();
		assertEquals(2, mockServer.getRequestCount());
	}

	@Test
	void testIndexIsOnlyOnceRequested_1() {
		enqueueIndex();
		enqueue404Responses(NUM_AVAILABLE_DAYS);
		dist.getAllAvailableKeys();
		assertEquals(NUM_AVAILABLE_DAYS + 1, mockServer.getRequestCount());
	}

	@Test
	void testIndexIsOnlyOnceRequested_2() {
		enqueueIndex();
		enqueue404Responses(NUM_AVAILABLE_DAYS);
		dist.requestAll(AVAILABLE_DATES);
		assertEquals(NUM_AVAILABLE_DAYS + 1, mockServer.getRequestCount());
	}

	@Test
	void testIndexIsOnlyOnceRequested_3() {
		enqueueIndex();
		enqueue404Responses(NUM_AVAILABLE_DAYS);
		dist.requestAllWithDate(AVAILABLE_DATES);
		assertEquals(NUM_AVAILABLE_DAYS + 1, mockServer.getRequestCount());
	}

	@Test
	void testIndexIsOnlyOnceRequested_4() {
		enqueueIndex();
		enqueue404Responses(NUM_AVAILABLE_DAYS);
		dist.getAllAvailableKeys();
		assertEquals(NUM_AVAILABLE_DAYS + 1, mockServer.getRequestCount());
	}

	@Test
	void testGetFull14Batch() throws InterruptedException {
		enqueueIndex();
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));

		final var full14Batch = dist.getFull14Batch();

		assertEquals("/exposures/at/index.json", mockServer.takeRequest().getPath());
		assertEquals("/exposures/at/1594581300/batch_full14-2655504-1.zip", mockServer.takeRequest().getPath());
	}

	@Test
	void testGetFull7Batch() throws InterruptedException {
		enqueueIndex();
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));

		final var full14Batch = dist.getFull7Batch();

		assertEquals("/exposures/at/index.json", mockServer.takeRequest().getPath());
		assertEquals("/exposures/at/1594581300/batch_full7-2656512-1.zip", mockServer.takeRequest().getPath());
	}
}