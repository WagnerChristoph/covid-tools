package util;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	public static final String EXPORT_BINARY_FILE_NAME = "export.bin";


	public static void unzip(ZipInputStream zis, BiConsumer<ZipEntry, ByteBuffer> entryConsumer) throws IOException {
		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {
			entryConsumer.accept(zipEntry, ByteBuffer.wrap(zis.readAllBytes()));
		}
			zis.closeEntry();
	}


	@Nullable
	public static ByteBuffer getExportBinaryFile(ZipInputStream zis) throws IOException{
		var wrapper = new Object(){ByteBuffer buffer;};
		ZipUtils.unzip(zis, (zipEntry, content) -> {
			if (zipEntry.getName().equals(EXPORT_BINARY_FILE_NAME)) {
				//ignore 16-byte header
//				ByteBuffer b = ByteBuffer.allocate(content.capacity() - 16);
//				wrapper.buffer = b.put(content.position(16));
				wrapper.buffer = content;
			}
		});
		return wrapper.buffer;
	}
}
