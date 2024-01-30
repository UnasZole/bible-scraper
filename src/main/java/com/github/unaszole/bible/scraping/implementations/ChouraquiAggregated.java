package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.scraping.Scraper;
import com.github.unaszole.bible.stream.ContextStream;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ChouraquiAggregated extends Scraper {
    private final ChouraquiSpiritualLand spiritualLand;
    private final TheoPlace theoPlace;
    public ChouraquiAggregated(Path cachePath, String[] flags) throws IOException {
        this.spiritualLand = new ChouraquiSpiritualLand(cachePath, flags);
        this.theoPlace = new TheoPlace(cachePath, new String[]{"chu"});
    }

    private List<BibleBook> getBookList() {
        List<BibleBook> bookList = spiritualLand.getBookList();
        bookList.add(bookList.indexOf(BibleBook.SONG), BibleBook.JOB);
        return bookList;
    }

    @Override
    protected ContextStream.Single getContextStreamFor(ContextMetadata rootContextMeta) {
        switch (rootContextMeta.type) {
            case BOOK:
                switch (rootContextMeta.book) {
                    case JOB:
                        return theoPlace.stream(rootContextMeta);
                    default:
                        return spiritualLand.stream(rootContextMeta);
                }
            case BIBLE:
                return autoGetBibleStream(getBookList());
        }
        return null;
    }
}
