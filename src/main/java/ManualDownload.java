import distribution.AbstractDistribution;
import distribution.de.DE_Distribution;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ManualDownload {
	public static void main(String[] args) {
		AbstractDistribution dt = new DE_Distribution();
		final LocalDate date = LocalDate.of(2020, 6, 24);
		final LocalDateTime datetime = LocalDateTime.of(2020, 6, 23, 17, 0);
//		final var jsonObject = KeyDownloader.IOUtils.TEKExportToJson(dt.getDiagnosisKeysForDate(date).get(), date);

//		Gson gson = new GsonBuilder().setPrettyPrinting().create();


//		System.out.println(gson.toJson(jsonObject));

//		System.out.println(dt.getDiagnosisKeysForDate(date).get().getKeysCount());
	}
}
