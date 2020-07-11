package download;

import java.time.LocalDate;
import java.util.List;

public interface DaysIndexable {
	List<LocalDate> getAvailableDays();
}
