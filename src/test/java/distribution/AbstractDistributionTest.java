package distribution;

import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AbstractDistributionTest {


	public static MockResponse getDiagnosisKeysTestFileMockResponse() throws IOException {
		return new MockResponse().setBody(new Buffer().write(Files.readAllBytes(Paths.get("src\\test\\files\\testExport-2-records-1-of-1.zip"))));
	}
}