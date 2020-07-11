package distribution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HoursIndexable {
	List<LocalDateTime> getAvailableHours(LocalDate day);
}
