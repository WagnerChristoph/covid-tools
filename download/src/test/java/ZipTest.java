import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTest {

	public static void main(String[] args) {
		readZipFileFromFile(Paths.get(""));


	}





	static void readZipFileFromFile(Path p) {
		try (ZipFile zipFile = new ZipFile(p.toFile())) {
			for(Iterator<? extends ZipEntry> it = zipFile.entries().asIterator(); it.hasNext(); ) {
				final ZipEntry next = it.next();
				System.out.printf("name: %s\tsize: %d\n", next.getName(), next.getSize());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
