package com.github.unaszole.bible.downloading;

import com.github.unaszole.bible.datamodel.valuetypes.Attachment;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class DownloadedAttachment implements Attachment {

    private final CachedDownloader downloader;
    private final URL url;

    public DownloadedAttachment(CachedDownloader downloader, URL url) {
        this.downloader = downloader;
        this.url = url;
    }

    @Override
    public String getName() {
        return url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
    }

    @Override
    public byte[] getBytes() {
        try {
            return Files.readAllBytes(downloader.getFile(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
