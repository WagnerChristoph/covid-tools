package download;

import okhttp3.Response;
import org.apache.commons.lang3.tuple.Pair;
import protobuf.TemporaryExposureKeyExport;

import java.time.LocalDate;
import java.util.Optional;

public interface Distribution {
	Optional<Pair<LocalDate, TemporaryExposureKeyExport>> getDiagnosisKeysForDayWithDay(LocalDate date);

	Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date);

	void executeRequest(String url, Callback<Response> callback);
}
