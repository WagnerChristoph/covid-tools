package download;

import com.google.protobuf.InvalidProtocolBufferException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import protobuf.TemporaryExposureKeyExport;
import util.ZipUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.zip.ZipInputStream;


public abstract class AbstractDistribution implements Distribution {
	private static final Logger logger = LogManager.getLogger(AbstractDistribution.class);
	protected final OkHttpClient client;

	public AbstractDistribution() {
		this.client = new OkHttpClient();
	}


	@Override
	public Optional<Pair<LocalDate, TemporaryExposureKeyExport>> getDiagnosisKeysForDayWithDay(LocalDate date) {
		return getDiagnosisKeysForDay(date)
				.flatMap(tek -> Optional.of(new ImmutablePair<>(date, tek)));
	}


	@Override
	public void executeRequest(String url, Callback<Response> callback) {
		Request r = new Request.Builder()
				.get()
				.url(url)
				.build();

		try (Response response = client.newCall(r).execute()) {
			if (response.isSuccessful()) {
				logger.debug("received response successfully");
				callback.onSuccess(response);
			}
			else{
				logger.error("unsuccessful response: {}", response.code());
			}
		} catch (IOException e) {
			callback.onError(e);
		}

	}


	public Optional<TemporaryExposureKeyExport> getKeyFile(String url) {
		logger.debug("requesting keys with: {}", url);
		var wrapper = new Object(){TemporaryExposureKeyExport keys = null;};

		Callback<Response> callback = new Callback<>() {
			@Override
			public void onSuccess(Response item) {
				if(item.code() == 204) {
					logger.warn("no content found");
					return;
				}
				try (ResponseBody responseBody = item.body();
						ZipInputStream zis = new ZipInputStream(responseBody.byteStream())){
					logger.debug("zip file size: {}", responseBody.contentLength());
					TemporaryExposureKeyExport keys = getKeysFromZip(zis);
					logger.info("extracted {} keys", keys == null ? "null" : keys.getKeysCount());
					wrapper.keys = keys;

				} catch (IOException e) {
					logger.error("error in response: {}", e.getMessage());
				}
			}

			@Override
			public void onError(Throwable t) {
				logger.error("error requesting keys: {}", t.getMessage());
			}
		};

		executeRequest(url, callback);
		return Optional.ofNullable(wrapper.keys);

	}




	@Nullable
	protected TemporaryExposureKeyExport getKeysFromZip(ZipInputStream zis) throws IOException {

		ByteBuffer binaryExportFile = ZipUtils.getExportBinaryFile(zis);
		if(binaryExportFile == null) {
			logger.error("could not extract binary export file from zip");
			return null;
		}

		try {
			//ignore 16-byte header
			return TemporaryExposureKeyExport.parseFrom(binaryExportFile.position(16));
		} catch (InvalidProtocolBufferException e) {
			logger.error("could not extract keys from zip file");
			return null;
		}
	}

	public Optional<TemporaryExposureKeyExport> extractKeysFromZipFile(Path zipfile) {
		TemporaryExposureKeyExport keys = null;
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipfile))) {
			keys = getKeysFromZip(zis);
		} catch (IOException e) {
			logger.error("error reading zipfile: {}", e.getMessage());
		}
		return Optional.ofNullable(keys);
	}
}
