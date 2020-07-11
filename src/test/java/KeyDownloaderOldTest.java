import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class  	KeyDownloaderOldTest {


	@Test
	public void createEmptyKeysFile() throws IOException {
		final Path fileName = Paths.get("myFile");
		final StringWriter stringWriter = new StringWriter();
		final KeyDownloader_old.IOFactory IOFactory = mock(KeyDownloader_old.IOFactory.class);
//		doReturn(stringWriter).when(fileWriterFactory.create(any(Path.class)));
		when(IOFactory.create(any(Path.class))).thenReturn(stringWriter);

//		new KeyDownloader(fileWriterFactory).createKeysFile(fileName);

		verify(IOFactory).create(fileName);
		assertEquals(stringWriter.toString(), "{\"dates\":[]}" + '\n');

	}

	@Test
	public void append() throws IOException {
		Files.writeString(Paths.get("temp"), "line1\n");
		System.out.println(Files.lines(Paths.get("temp")).count());
		try(BufferedWriter bos =  Files.newBufferedWriter(Paths.get("temp"), StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
			bos.write("line2\n");
		}
		System.out.println(Files.lines(Paths.get("temp")).count());


	}

}