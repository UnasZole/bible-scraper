package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.scraping.generic.index.providers.Container;
import com.github.unaszole.bible.scraping.generic.index.providers.ContextProvider;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import org.crosswire.jsword.versification.BibleBook;

import java.util.*;
import java.util.stream.Stream;

/**
 * Specifies the contents of a book to retrieve from the source.
 * <p>
 * Pages defined at book level, if any, will be scraped to initialise this book's context - else an empty book context
 * will be initialised.
 * If {@link #chapters} are specified, the corresponding pages will be scraped and appended at the end of the book context.
 */
public class Book extends PagesContainer {

    /**
     * The OSIS ID of the book.
     */
    public BibleBook osis;

    /**
     * The names under which this book is referenced within the bible. This is used when parsing cross-references.
     */
    public List<String> names;

    /**
     * Description of this book's contents as a sequence of chapters.
     * May be left empty if the {@link #pages} already contain all chapters of the book.
     */
    public List<ChapterSeq> chapters;

    public Container asContainer() {
        PatternConfig bookPatternConfig = new PatternConfig(
                Map.of("bookId", osis.getOSIS()),
                Map.of(),
                "book"
        );

        return new Container(
                new ContextProvider(
                        BibleContainers.BOOK.name(),
                        Map.of(BibleIdFields.BOOK.name(), "Id"),
                        "Id"
                ),
                asPatternConfig().defaultedBy(bookPatternConfig),
                Stream.concat(
                        getPatternPages(),
                        Optional.ofNullable(chapters).stream()
                                .flatMap(Collection::stream)
                                .map(ChapterSeq::asIteration)
                ).toList()
        );
    }
}
