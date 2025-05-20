package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.scraping.ScrapingUtils;
import org.crosswire.jsword.versification.BibleBook;
import picocli.CommandLine;

import java.util.Optional;

public class WantedMetadataArgument {

    @CommandLine.Option(names={"--fullBible"}, defaultValue = "false", description = "Extract the full Bible.")
    private boolean fullBible;

    @CommandLine.Option(names={"--book", "-b"}, description = "Extract from a specific book by specifying its OSIS ID.")
    private Optional<String> book;

    @CommandLine.Option(names={"--chapter", "-c"}, description = "Extract from a specific chapter in the book.")
    private Optional<Integer> chapter;

    public ContextMetadata get() {
        if(fullBible) {
            return ScrapingUtils.forBible();
        }

        if(book.isPresent()) {
            if(chapter.isPresent()) {
                return ScrapingUtils.forChapter(BibleBook.fromOSIS(book.get()), chapter.get());
            }
            return ScrapingUtils.forBook(BibleBook.fromOSIS(book.get()));
        }

        throw new IllegalArgumentException("If not providing a specific book, please specify --fullBible .");
    }
}
