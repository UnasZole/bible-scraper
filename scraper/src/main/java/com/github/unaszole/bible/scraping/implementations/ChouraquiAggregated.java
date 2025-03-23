package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.scraping.Scraper;
import com.github.unaszole.bible.stream.ContextStream;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class ChouraquiAggregated extends Scraper {

    public static Help getHelp(String[] inputs) {
        Help slHelp = ChouraquiSpiritualLand.getHelp(inputs);

        return new Help("Bible d'André Chouraqui complète, agrégée de plusieurs sources",
                slHelp.inputDescriptions, slHelp.inputsValid
        );
    }

    private final ChouraquiSpiritualLand spiritualLand;
    private final TheoPlace theoPlace;
    public ChouraquiAggregated(Path cachePath, String[] inputs) throws IOException {
        this.spiritualLand = new ChouraquiSpiritualLand(cachePath, inputs);
        this.theoPlace = new TheoPlace(cachePath, new String[]{"chu"});
    }

    private List<BibleBook> getBookList() {
        List<BibleBook> bookList = spiritualLand.getBookList();
        bookList.add(bookList.indexOf(BibleBook.SONG), BibleBook.JOB);
        return bookList;
    }

    @Override
    public DocumentMetadata getMeta() {
        return new DocumentMetadata(Locale.FRENCH, "freCHUagg", "Bible d'André Chouraqui", spiritualLand.getMeta().refSystem);
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
