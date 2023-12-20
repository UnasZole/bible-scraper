package com.github.unaszole.bible;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class CachedDownloader {
	
	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	private static final MessageDigest HASHER = getMessageDigest();
	
	private static String getHash(URL url) {
		Formatter formatter = new Formatter();
		for(byte b : HASHER.digest(url.toString().getBytes())) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
	
	private final Path cacheDirectory;
	
	public CachedDownloader(Path cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}
	
	public Path getFile(URL url) throws IOException {
		String hash = getHash(url);
		Path targetPath = cacheDirectory.resolve(hash);
		
		if(Files.exists(targetPath)) {
			// File is already present, return it.
			return targetPath;
		}
		
		// File is missing, download it.
		ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
		FileChannel outChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
		
		return targetPath;
	}
}