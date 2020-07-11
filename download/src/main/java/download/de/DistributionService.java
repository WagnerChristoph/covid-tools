package download.de;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DistributionService {

	@GET("/version/{version}/diagnosis-keys/country/{country}/date")
	ResponseBody getAvailableDays(@Path("version") String version, @Path("country") String country);

}
