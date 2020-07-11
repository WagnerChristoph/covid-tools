package distribution.ch;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class Test {
	public static void main(String[] args) {
		System.out.println((LocalDate.of(2020, 6, 12).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000));
	}
}
