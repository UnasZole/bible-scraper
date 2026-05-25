package com.github.unaszole.bible.scraping.generic;

import com.github.unaszole.bible.datamodel.valuetypes.Attachment;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.downloading.DownloadedAttachment;
import org.crosswire.jsword.versification.BibleBook;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public interface ValueTypeMapper<ValueClass> {
    Optional<ValueClass> map(String str);

    record BibleBookMapper(Map<String, BibleBook> bookReferences) implements ValueTypeMapper<BibleBook> {
        @Override
        public Optional<BibleBook> map(String str) {
            return Optional.ofNullable(bookReferences.get(str));
        }
    }

    record UriMapper(URI baseUri) implements ValueTypeMapper<URI> {
        @Override
        public Optional<URI> map(String str) {
            return Optional.ofNullable(baseUri)
                    .map(b -> baseUri.resolve(URI.create(str)));
        }
    }

    record AttachmentMapper(CachedDownloader downloader, URI baseUri) implements ValueTypeMapper<Attachment> {
        @Override
        public Optional<Attachment> map(String str) {
            try {
                return Optional.of(new DownloadedAttachment(downloader, Optional.ofNullable(baseUri)
                        .map(b -> b.resolve(URI.create(str)))
                        .orElse(URI.create(str))
                        .toURL()
                ));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
