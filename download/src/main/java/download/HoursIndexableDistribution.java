package download;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HoursIndexableDistribution extends Distribution {
	List<LocalDateTime> getAvailableHours(LocalDate day);
}
