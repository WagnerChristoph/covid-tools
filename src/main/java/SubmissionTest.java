import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import com.google.protobuf.ByteString;
import okhttp3.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class SubmissionTest {

	public static final String SUBMISSION_URL = "https://submission.coronawarn.app/version/v1/diagnosis-keys";

	private final OkHttpClient client;

	public SubmissionTest() {
		this.client = new OkHttpClient();
	}


	private Optional<String> submitAndGetResponse(){
		SubmissionPayload payload = SubmissionPayload.newBuilder()
													 .addKeys(TemporaryExposureKey.newBuilder()
																				  .setKeyData(ByteString.copyFromUtf8("aaabbbcccdddeeff"))
																				  .setRollingPeriod(55)
																				  .setTransmissionRiskLevel(55)
																				  .setRollingStartIntervalNumber(55))
													 .build();

		RequestBody body = RequestBody.create(payload.toByteArray(), MediaType.get("application/x-protobuf"));
		Request r = createRequestWithHeaders(true, UUID.randomUUID().toString())
				.url(SUBMISSION_URL)
				.post(body)
				.build();
		try (Response response = client.newCall(r).execute()) {
			 return Optional.of(response.code() + " " + response.message());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return Optional.empty();
	}

	public static void main(String[] args) {
		SubmissionTest st = new SubmissionTest();
		st.submitAndGetResponse().ifPresent(System.out::println);



	}

	private Request.Builder createRequestWithHeaders(boolean fake, String authorization) {
		return new Request.Builder()
				.addHeader("cwa-fake", fake ? "1" : "0")
				.addHeader("cwa-authorization", authorization);
	}

}
