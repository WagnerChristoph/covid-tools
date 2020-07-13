package download;

import download.at.AT_Distribution;
import download.ch.CH_Distribution;
import download.de.DE_Distribution;

public class DistributionFactory {
	public Distribution getDistribution(DistributionType type) {
		return switch (type) {
			case DE -> new DE_Distribution();
			case CH -> new CH_Distribution();
			case AT -> new AT_Distribution();
		};
	}
}
