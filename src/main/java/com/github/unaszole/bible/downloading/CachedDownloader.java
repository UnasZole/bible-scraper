package com.github.unaszole.bible.downloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CachedDownloader {

	private static final Logger LOG = LoggerFactory.getLogger(CachedDownloader.class);
	
	private final Path cacheDirectory;
	
	public CachedDownloader(Path cacheDirectory) throws IOException {
		this.cacheDirectory = cacheDirectory;
		Files.createDirectories(cacheDirectory);
	}

	/**
	 *
	 * @param source The source file to open.
	 * @return The path to a local working copy of this source file.
	 * @throws IOException If any error prevented reading the source file.
	 */
	public Path getFile(SourceFile source) throws IOException {
		String hash = source.getHash();
		Path targetPath = cacheDirectory.resolve(hash);

		if(Files.exists(targetPath)) {
			// File is already present, return it.
			return targetPath;
		}

		LOG.debug("Downloading from {}", source);

		// File is missing, download it.
		ReadableByteChannel inChannel = Channels.newChannel(source.openStream());
		FileChannel outChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
		
		return targetPath;
	}

	public Path getFile(URL url) throws IOException {
		return getFile(new HttpSourceFile(url));
	}
}