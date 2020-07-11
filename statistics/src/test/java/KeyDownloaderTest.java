import org.junit.jupiter.api.Test;
import util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class KeyDownloaderTest {



	@Test
	public void testFiles() {
		final Path path = Paths.get("");
		IOUtils utils = new IOUtils(path);
		utils.getExistingDates().keySet()
			 .forEach(System.out::println);
	}

}