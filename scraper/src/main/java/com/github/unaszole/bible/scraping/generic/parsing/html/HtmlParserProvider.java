package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.parsing.ParserCore;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class HtmlParserProvider implements TextParser.Provider<Element> {

    public List<ElementParser> elements;
    public List<ElementExternalParser> externalParsers;

    @Override
    public Iterator<Element> iterate(InputStream input) throws IOException {
        return Jsoup.parse(input, null, "").stream().iterator();
    }

    @Override
    public ParserCore<Element> getParser(ContextualData contextualData) {
        return new ConfiguredHtmlParser(elements, externalParsers, contextualData);
    }
}
