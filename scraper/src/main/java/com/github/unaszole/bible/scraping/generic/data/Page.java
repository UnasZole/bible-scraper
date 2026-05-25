package com.github.unaszole.bible.scraping.generic.data;

import com.github.unaszole.bible.scraping.generic.index.providers.PatternPage;

import java.util.Optional;

public class Page extends PatternContainer {

    public Optional<PatternPage> asPatternPage() {
        return Optional.of(new PatternPage(asPatternConfig()));
    }
}
