package download;

import com.google.gson.JsonArray;
import download.ch.CH_Distribution;
import download.de.DE_Distribution;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static download.DistributionType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class KeyDownloaderTest {
	public static final LocalDate EXAMPLE_DATE = LocalDate.of(2020, 9, 8);


	private final IOUtils dummyIoUtils = Mockito.mock(IOUtils.class);
	private final IOUtils.IOUtilsFactory dummyIoUtilsFactory = Mockito.mock(IOUtils.IOUtilsFactory.class);

	private MockWebServer mockServer;


	@BeforeEach
	void setUp() throws IOException {
		when(dummyIoUtilsFactory.create(any())).thenReturn(dummyIoUtils);
		when(dummyIoUtils.persistTEKs(anyMap())).thenAnswer(i -> ((Map<?,?>)i.getArgument(0)).size());

		mockServer = new MockWebServer();
		var dispatcher = new QueueDispatcher();
		dispatcher.setFailFast(true);
		mockServer.setDispatcher(dispatcher);
		mockServer.start();
	}


	private DistributionFactory returnDistribution(Distribution dt) {
		DistributionFactory mock = mock(DistributionFactory.class);
		when(mock.getDistribution(any())).thenReturn(dt);
		return mock;
	}



	@Test
	void testDaysIndexableWithAvailableNewFilesAndNoLocalFiles() {
		when(dummyIoUtils.getExistingDateFiles()).thenReturn(Set.of());
		final KeyDownloader keyDownloader = new KeyDownloader(returnDistribution(
				new DE_Distribution(mockServer.url("").toString())), dummyIoUtilsFactory);

		List<LocalDate> days = List.of(EXAMPLE_DATE,
				EXAMPLE_DATE.plusDays(1),
				EXAMPLE_DATE.plusDays(2),
				EXAMPLE_DATE.plusDays(3));
		final JsonArray jsonDateArr = new JsonArray();
		days.stream()
			.map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
			.forEach(jsonDateArr::add);
		mockServer.enqueue(new MockResponse().setBody(jsonDateArr.toString()));


		keyDownloader.downloadCountryKeys(DE);

		//additional index request
		assertEquals(days.size() + 1, mockServer.getRequestCount());
	}

	@Test
	void testDaysIndexableWithAvailableNewFilesAndExistingLocalFiles() {
		List<LocalDate> days = List.of(EXAMPLE_DATE,
				EXAMPLE_DATE.plusDays(1),
				EXAMPLE_DATE.plusDays(2),
				EXAMPLE_DATE.plusDays(3));
		// 2 local files
		when(dummyIoUtils.getExistingDates()).thenReturn(Map.of(days.get(0), Paths.get(""),
																days.get(1), Paths.get("")));
		final KeyDownloader keyDownloader = new KeyDownloader(returnDistribution(
				new DE_Distribution(mockServer.url("").toString())), dummyIoUtilsFactory);

		final JsonArray jsonDateArr = new JsonArray();
		days.stream()
			.map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
			.forEach(jsonDateArr::add);
		mockServer.enqueue(new MockResponse().setBody(jsonDateArr.toString()));


		keyDownloader.downloadCountryKeys(DE);

		//2 local files, 2 newly requested
		assertEquals(3, mockServer.getRequestCount());
	}

	@Test
	void testNotDaysIndexableWithNoLocalFiles() {
		when(dummyIoUtils.getExistingDateFiles()).thenReturn(Set.of());
		final KeyDownloader keyDownloader = new KeyDownloader(returnDistribution(
				new CH_Distribution(mockServer.url("").toString())), dummyIoUtilsFactory);

		keyDownloader.downloadCountryKeys(CH);

		//should request last 14 days
		assertEquals(14, mockServer.getRequestCount());

	}

	@Test
	void testNotDaysIndexableWithExistingLocalFiles() {
		LocalDate begin = LocalDate.now();
		int numNewRequests = 5;

		//oldest existing file is (now - numNewRequests) days old, so expect numNewRequests new requests
		Map<LocalDate, Path> map = new HashMap<>();
		for (int i = 1; i < 5; i++) {
			//also, assure that existing dates are correctly ordered (and latest existing file is used)
			map.put(begin.minusDays(numNewRequests + i), Paths.get(""));
		}


		when(dummyIoUtils.getExistingDates()).thenReturn(map);
		final KeyDownloader keyDownloader = new KeyDownloader(returnDistribution(
				new CH_Distribution(mockServer.url("").toString())), dummyIoUtilsFactory);

		keyDownloader.downloadCountryKeys(CH);

		assertEquals(numNewRequests, mockServer.getRequestCount());

	}

}