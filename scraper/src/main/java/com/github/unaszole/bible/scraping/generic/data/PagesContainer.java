package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.scraping.generic.index.providers.PatternPage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class PagesContainer extends PatternContainer {

    public List<Page> pages;

    public List<Page> getPages() {
        if(pages != null) {
            // If pages are explicitly specified, return them.
            return pages;
        }

        // Else, return an implicit page.
        return List.of(new Page());
    }

    public Stream<PatternPage> getPatternPages() {
        return getPages().stream().map(Page::asPatternPage).flatMap(Optional::stream);
    }
}
