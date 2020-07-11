import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PaddingTest {

	public static byte[] getRandomByteArray() {
		byte[] randomBytes = new byte[8];
		new Random().nextBytes(randomBytes);
		return randomBytes;
	}



	static List<byte[]> generatePadding(List<byte[]> input) {
		List<byte[]> result = new ArrayList<>(input);


		return result;
	}



	@Test
	public void testt() {
		List<byte[]> input = List.of(new byte[]{1,2,3}, new byte[]{4,5,6}, new byte[]{7,8,9});
		System.out.println("length input: " + input.size());
		List<byte[]> output=  generatePadding(input);
		System.out.println("length output: " + output.size());


		System.out.println("IntStream.range(1, 10)\n\t\t\t\t .count() = " + IntStream.range(1, 10)
																					 .count());
	}

}
