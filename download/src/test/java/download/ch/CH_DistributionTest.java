package download.ch;

import download.AbstractDistributionTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protobuf.TemporaryExposureKeyExport;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CH_DistributionTest {

	public static final LocalDate EXAMPLE_DATE = LocalDate.of(2020, 7, 1);


	private MockWebServer mockServer;
	private CH_Distribution dist;

	@BeforeEach
	public void setUp() throws IOException {
		mockServer = new MockWebServer();
		mockServer.start();
		String baseUrl = mockServer.url("").toString();
		dist = new CH_Distribution(baseUrl);
	}

	@Test
	void testGetDiagnosisKeysForDay() throws IOException, InterruptedException {
		mockServer.enqueue(AbstractDistributionTest.getDiagnosisKeysTestFileMockResponse());

		final TemporaryExposureKeyExport tekExport = dist.getDiagnosisKeysForDay(EXAMPLE_DATE).orElseThrow();

		assertEquals(2, tekExport.getKeysCount());
		assertEquals("/v1/gaen/exposed/1593561600000", mockServer.takeRequest().getPath());

	}

	@Test
	void testGetDiagnosisKeysForDay_NotFound() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));

		final Optional<TemporaryExposureKeyExport> optional = dist.getDiagnosisKeysForDay(EXAMPLE_DATE);

		assertTrue(optional.isEmpty());
	}

	@AfterEach
	void tearDown() throws IOException{
		mockServer.shutdown();
	}
}