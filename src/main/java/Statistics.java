import com.google.gson.Gson;
import model.TEKExport;
import util.IOUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Statistics {


	//todo: path
	public static final Statistics DEFAULT = new Statistics("");

	private final Path keyDir;
	private final IOUtils ioUtils;

	public Statistics(String keyDir) {
		this.keyDir = Paths.get(Objects.requireNonNull(keyDir));
		ioUtils = new IOUtils(this.keyDir);
	}


	private Stream<TEKExport.TEK> streamAllKeys() {
		return ioUtils.getExistingDateFiles().stream()
					  .map(ioUtils::safelyDeserialize)
					  .flatMap(Optional::stream)
					  .flatMap(tekExport -> tekExport.getKeys().stream());
	}



	private void similarKeyStatistics2() {
		Gson gson = new Gson();
		final Map<Integer, Map<Integer, List<TEKExport.TEK>>> multimap = streamAllKeys()
				.collect(groupingBy(TEKExport.TEK::getRollingStartIntervalNumber,
						groupingBy(TEKExport.TEK::getTransmissionRiskLevel)));

//		final Map<Long, Long> collect = multimap.entrySet().stream()
//												.flatMap(entry -> entry.getValue().entrySet().stream())
//												.map(Map.Entry::getValue)
//												.collect(groupingBy(Function.identity(), counting()));
		for (var outer : multimap.entrySet()) {
			System.out.print(String.format("rolling start interval: %d\n", outer.getKey()));
			for (var inner : outer.getValue().entrySet()) {
				System.out.print(String.format("transmission risk level: %d\t times: %d\n", inner.getKey(), inner.getValue().size()).indent(1));
				for (var tek : inner.getValue()) {
					System.out.print(gson.toJson(tek).indent(2));
				}
				System.out.println();
			}
			System.out.println();
		}


	}

		private void similarKeyStatistics() {
		final Map<Integer, Map<Integer, Long>> multimap = streamAllKeys()
				.collect(groupingBy(TEKExport.TEK::getRollingStartIntervalNumber,
						groupingBy(TEKExport.TEK::getTransmissionRiskLevel, counting())));

		final Map<Long, Long> collect = multimap.entrySet().stream()
												.flatMap(entry -> entry.getValue().entrySet().stream())
												.map(Map.Entry::getValue)
												.collect(groupingBy(Function.identity(), counting()));






//												.mapToLong(Map.Entry::getValue)
//												.collect(HashMap::new,
//														(map, value) -> map.merge(value, 1L, Long::sum),
//														(map1, map2) -> map2.forEach(
//																(key, value) -> map1.merge(key, value, Long::sum)
//												));
		collect.entrySet().stream()
			   .sorted(Map.Entry.comparingByKey())
			   .forEach(entry -> System.out.println(String.format("%d similar keys: %d times", entry.getKey(), entry.getValue())));

	}


	public static void main(String[] args) {
		DEFAULT.similarKeyStatistics2();
	}


}
