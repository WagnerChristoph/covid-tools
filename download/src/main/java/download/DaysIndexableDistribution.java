package download;

import protobuf.TemporaryExposureKeyExport;

import java.time.LocalDate;
import java.util.List;

public interface DaysIndexableDistribution extends Distribution{
	List<LocalDate> getAvailableDays();

	default List<TemporaryExposureKeyExport> getAllAvailableKeys() {
		return requestAll(getAvailableDays());
	}
}
