import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import com.google.protobuf.InvalidProtocolBufferException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.ZipUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.zip.ZipInputStream;

public class GetConfig {

	public static final String EXPORT_BINARY_FILE_NAME = "export.bin";
	public static final String CONFIG_URL = "https://svc90.main.px.t-online.de/version/v1/configuration/country/DE/app_config";

	private static final Logger logger = LogManager.getLogger(GetConfig.class);

	private final OkHttpClient client;

	public GetConfig() {
		this.client = new OkHttpClient();
	}

	private Optional<ApplicationConfiguration> getConfig() {
		ApplicationConfiguration configuration = null;
		Request r = new Request.Builder()
				.get()
				.url(CONFIG_URL)
				.build();

		try (Response response = client.newCall(r).execute();
			 ZipInputStream zis = new ZipInputStream(response.body().byteStream())) {
			if (response.isSuccessful()) {

				ByteBuffer binaryExportFile = ZipUtils.getExportBinaryFile(zis);
				if(binaryExportFile == null) {
					logger.error("could not extract binary export file from zip");
				} else{
					try{
						configuration = ApplicationConfiguration.parseFrom(binaryExportFile);
					} catch (InvalidProtocolBufferException e) {
						logger.error("could not parse config from zip file");
					}
				}

//				ZipEntry zipEntry;
//				while ((zipEntry = zis.getNextEntry()) != null) {
//					if (zipEntry.getName().equals(EXPORT_BINARY_FILE_NAME)) {
//						configuration = ApplicationConfiguration.parseFrom(zis.readAllBytes());
//					}
//					zis.closeEntry();
//				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return Optional.ofNullable(configuration);
	}


	public static void main(String[] args) {
		GetConfig st = new GetConfig();
		st.getConfig().ifPresent(System.out::println);

	}

}
