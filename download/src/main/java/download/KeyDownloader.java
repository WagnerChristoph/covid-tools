package download;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import protobuf.TemporaryExposureKeyExport;
import util.IOUtils;
import util.IOUtils.IOUtilsFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;

public class KeyDownloader {

	private static final Logger logger = LogManager.getLogger(KeyDownloader.class);

	//default current working dir
	private Path baseKeyDir = Paths.get("");
	private final DistributionFactory distributionFactory;
	private final IOUtilsFactory ioUtilsFactory;


	//for testing mostly
	public KeyDownloader(DistributionFactory distributionFactory, IOUtilsFactory ioUtilsFactory) {
		this.distributionFactory = distributionFactory;
		this.ioUtilsFactory = ioUtilsFactory;
	}

	public KeyDownloader(Path baseKeyDir) {
		this.baseKeyDir = baseKeyDir;
		this.distributionFactory = new DistributionFactory();
		this.ioUtilsFactory = new IOUtilsFactory();
	}


	public void downloadCountryKeys(DistributionType type){
		logger.info("downloading keys for {}", type.toString());
		downloadKeys(distributionFactory.getDistribution(type), ioUtilsFactory.create(baseKeyDir.resolve(type.toString().toLowerCase())));
	}


	/**
	 * Downloads key files from specified {@code Distribution} returned by {@code getDateToRequest()} and saves them with specified {@code IOUtils}.
	 * @param distribution The distribution to download the keys from.
	 * @param ioUtils The IOUtils to save the newly downloaded key files.
	 */
	public void downloadKeys(@NotNull Distribution distribution, @NotNull IOUtils ioUtils) {
		final Set<LocalDate> existingDates = ioUtils.getExistingDates().keySet();
		logger.info("found {} existing files", existingDates.size());

		Set<LocalDate> datesToRequest = getDateToRequest(distribution, existingDates);
		logger.info("requesting {} new files", datesToRequest.size());

		//request
		final Map<LocalDate, TemporaryExposureKeyExport> newTEKs = distribution.requestAllWithDate(datesToRequest);
		logger.info("received {} new files", newTEKs.size());


		final int newFilecount = ioUtils.persistTEKs(newTEKs);
		logger.info("wrote {} new files", newFilecount);

	}

	/**
	 * Get the dates to request depending on already existing key files.
	 * If {@code distribution} is {@code DaysIndexableDistribution}, then the available files on the server are requested and the missing/not present dates are returned.
	 * Else the dates beginning  the day after the last existing key file until yesterday are returned. In case of an error or no existing files, the last 14 days are returned.
	 *
	 * @param distribution The distribution to download the keys from.
	 * @param existingDates Set of dates for which keys are already present.
	 * @return Set of dates to request.
	 */
	@NotNull
	private Set<LocalDate> getDateToRequest(Distribution distribution, Set<LocalDate> existingDates) {
		if(distribution instanceof DaysIndexableDistribution) {
			//if we can list the available dates, get them
			final Set<LocalDate> availableDates = new HashSet<>(((DaysIndexableDistribution) distribution).getAvailableDays());
			logger.info("found {} available dates on server", availableDates.size());

			availableDates.removeAll(existingDates);
			return availableDates;

		}else {
			//request from latest existing date on or alternatively last 14 days
			final LocalDate startingDate = existingDates.stream()
														.max(naturalOrder())
														.map(d-> {
															logger.debug("oldest found date: " + d.format(DateTimeFormatter.ISO_LOCAL_DATE));
															return d;
														})
														//don't include last existing date
														.map(date -> date.plusDays(1))
																				//exclusive
														.orElse(LocalDate.now().minusDays(14));
			logger.info("requesting new files from {} on", startingDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

			return startingDate
					//dont include today
					.datesUntil(LocalDate.now())
					.collect(Collectors.toSet());
		}
	}



	public static void main(String[] args) {
		OptionParser parser = new OptionParser();
		final OptionSpec<String> countriesOption = parser.acceptsAll(List.of("c", "countries"), "comma-separated list of countries to download, currently supported: de, ch").withRequiredArg().describedAs("country-code").ofType(String.class).withValuesSeparatedBy(',').required();
		final OptionSpec<String> dirOption = parser.acceptsAll(List.of("d", "directory"), "path to download the keys to. default: current directory").withRequiredArg().describedAs("path").ofType(String.class);
		final OptionSpec<Void> helpOption = parser.acceptsAll(List.of("h", "help"), "prints this help screen").forHelp();

		final OptionSet options = parser.parse(args);


		if(options.has(helpOption)) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException ignored) {
			}
			return;
		}

		EnumSet<DistributionType> downloadTypes = EnumSet.noneOf(DistributionType.class);
		for (String s : options.valuesOf(countriesOption)) {
			try {
				downloadTypes.add(DistributionType.valueOf(s.toUpperCase()));
			} catch (IllegalArgumentException e) {
				logger.error("{} is not a supported county", s);
			}
		}

		logger.info("downloading keys for: {}", downloadTypes.stream().map(DistributionType::toString).collect(Collectors.joining(",")) );


		Path dirPath = Paths.get("");
		if(options.has(dirOption)) {
			String dirPathString = options.valueOf(dirOption);

//			Files.isWritable(dirPathString)

			dirPath = Paths.get(dirPathString);
			logger.info("downloading to: {}", dirPath.toString());
		}


		KeyDownloader keyDownloader = new KeyDownloader(dirPath);
		downloadTypes.forEach(keyDownloader::downloadCountryKeys);



	}

}
