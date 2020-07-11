import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IT_Test {


	@Test
	public void tryParse() {
		//todo: cleanup
		try (InputStream is = Files.newInputStream(Paths.get(""))) {
			final ByteBuffer wrap = ByteBuffer.wrap(is.readAllBytes());
			TemporaryExposureKeyExport.parseFrom(wrap.position(16));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
