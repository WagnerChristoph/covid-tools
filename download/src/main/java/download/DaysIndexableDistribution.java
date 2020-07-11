package download;

import java.time.LocalDate;
import java.util.List;

public interface DaysIndexableDistribution extends Distribution{
	List<LocalDate> getAvailableDays();
}
