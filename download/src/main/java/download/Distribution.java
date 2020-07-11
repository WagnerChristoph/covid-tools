package download;

import okhttp3.Response;
import protobuf.TemporaryExposureKeyExport;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Distribution {

	/**
	 * Request the temporary exposure key export file for a date.
	 * @param date The date to request the temporary exposure key export file for.
	 * @return If successful, the {@code Optional} contains the temporary exposure key export file.
	 */
	Optional<TemporaryExposureKeyExport> getDiagnosisKeysForDay(LocalDate date);

	/**
	 * Request temporary exposure key export files for specified dates.
	 * @param dates Collection of dates to request temporary exposure key export files for.
	 * @return A list of successfully retrieved temporary exposure key export file.
	 */
	List<TemporaryExposureKeyExport> requestAll(Collection<LocalDate> dates);

	/**
	 * Request temporary exposure key export files for specified dates and return them as entries of a map.
	 * @param dates Collection of dates to request temporary exposure key export files for.
	 * @return	Map whose entries consist of a date and its corresponding temporary exposure key export file. No
	 * entry is created if request is not successful.
	 */
	Map<LocalDate, TemporaryExposureKeyExport> requestAllWithDate(Collection<LocalDate> dates);


	void executeRequest(String url, Callback<Response> callback);
}
