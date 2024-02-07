package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.CachedDownloader;
import com.github.unaszole.bible.scraping.Parser;
import com.github.unaszole.bible.scraping.ParsingUtils;
import com.github.unaszole.bible.scraping.Scraper;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import com.github.unaszole.bible.stream.StreamUtils;
import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class TheoPlace extends Scraper {

    public static Help getHelp(String[] inputs) {
        return new Help("Multiples bibles de theo.place", List.of(
                Map.entry("bible (required)", "La bible à lire de theo.place")
        ), inputs.length == 1);
    }

    private static final String CHAPTER_URL = "https://theo.place/bible-{BIBLE}-{BOOK_NB}-{CHAPTER_NB}";
    private static final String BOOK_INTRO_URL = "https://theo.place/intro-livre-{BIBLE}-{BOOK_NB}-{BOOK_NAME}";

    private static class BookRef {
        private final int bookNb;
        private final String bookName;
        public final int nbChapters;

        private BookRef(int bookNb, String bookName, int nbChapters) {
            this.bookNb = bookNb;
            this.bookName = bookName;
            this.nbChapters = nbChapters;
        }

        public String getBookIntroUrl(String bible) {
            return BOOK_INTRO_URL
                    .replace("{BIBLE}", bible)
                    .replace("{BOOK_NB}", Integer.toString(bookNb))
                    .replace("{BOOK_NAME}", bookName);
        }

        public String getChapterUrl(String bible, int chapterNb) {
            return CHAPTER_URL
                    .replace("{BIBLE}", bible)
                    .replace("{BOOK_NB}", Integer.toString(bookNb))
                    .replace("{CHAPTER_NB}", Integer.toString(chapterNb));
        }
    }

    private static final Map<BibleBook, BookRef> BOOKS = new LinkedHashMap<>();
    static {
        BOOKS.put(BibleBook.GEN, new BookRef(1, "genese", 50));
        BOOKS.put(BibleBook.EXOD, new BookRef(2, "exode", 40));
        BOOKS.put(BibleBook.LEV, new BookRef(3, "levitique", 27));
        BOOKS.put(BibleBook.NUM, new BookRef(4, "nombres", 36));
        BOOKS.put(BibleBook.DEUT, new BookRef(5, "deuteronome", 34));
        BOOKS.put(BibleBook.JOSH, new BookRef(6, "josue", 24));
        BOOKS.put(BibleBook.JUDG, new BookRef(7, "juges", 21));
        BOOKS.put(BibleBook.RUTH, new BookRef(8, "ruth", 4));
        BOOKS.put(BibleBook.SAM1, new BookRef(9, "1samuel", 31));
        BOOKS.put(BibleBook.SAM2, new BookRef(10, "2samuel", 24));
        BOOKS.put(BibleBook.KGS1, new BookRef(11, "1rois", 22));
        BOOKS.put(BibleBook.KGS2, new BookRef(12, "2rois", 25));
        BOOKS.put(BibleBook.CHR1, new BookRef(13, "1chroniques", 29));
        BOOKS.put(BibleBook.CHR2, new BookRef(14, "2chroniques", 36));
        BOOKS.put(BibleBook.EZRA, new BookRef(15, "esdras", 10));
        BOOKS.put(BibleBook.NEH, new BookRef(16, "nehemie", 13));
        BOOKS.put(BibleBook.ESTH, new BookRef(17, "esther", 10));
        BOOKS.put(BibleBook.JOB, new BookRef(18, "job", 42));
        BOOKS.put(BibleBook.PS, new BookRef(19, "psaumes", 150));
        BOOKS.put(BibleBook.PROV, new BookRef(20, "proverbes", 31));
        BOOKS.put(BibleBook.ECCL, new BookRef(21, "ecclesiaste", 12));
        BOOKS.put(BibleBook.SONG, new BookRef(22, "cantique", 8));
        BOOKS.put(BibleBook.ISA, new BookRef(23, "esaie", 66));
        BOOKS.put(BibleBook.JER, new BookRef(24, "jeremie", 52));
        BOOKS.put(BibleBook.LAM, new BookRef(25, "lamentations", 5));
        BOOKS.put(BibleBook.EZEK, new BookRef(26, "ezechiel", 48));
        BOOKS.put(BibleBook.DAN, new BookRef(27, "daniel", 12));
        BOOKS.put(BibleBook.HOS, new BookRef(28, "osee", 14));
        BOOKS.put(BibleBook.JOEL, new BookRef(29, "jo%C3%ABl", 3));
        BOOKS.put(BibleBook.AMOS, new BookRef(30, "amos", 9));
        BOOKS.put(BibleBook.OBAD, new BookRef(31, "abdias", 1));
        BOOKS.put(BibleBook.JONAH, new BookRef(32, "jonas", 4));
        BOOKS.put(BibleBook.MIC, new BookRef(33, "michee", 7));
        BOOKS.put(BibleBook.NAH, new BookRef(34, "nahum", 3));
        BOOKS.put(BibleBook.HAB, new BookRef(35, "habakuk", 3));
        BOOKS.put(BibleBook.ZEPH, new BookRef(36, "sophonie", 3));
        BOOKS.put(BibleBook.HAG, new BookRef(37, "aggee", 37));
        BOOKS.put(BibleBook.ZECH, new BookRef(38, "zacharie", 14));
        BOOKS.put(BibleBook.MAL, new BookRef(39, "malachie", 4));
        BOOKS.put(BibleBook.MATT, new BookRef(40, "matthieu", 28));
        BOOKS.put(BibleBook.MARK, new BookRef(41, "marc", 16));
        BOOKS.put(BibleBook.LUKE, new BookRef(42, "luc", 24));
        BOOKS.put(BibleBook.JOHN, new BookRef(43, "jean", 21));
        BOOKS.put(BibleBook.ACTS, new BookRef(44, "actes", 28));
        BOOKS.put(BibleBook.ROM, new BookRef(45, "romains", 16));
        BOOKS.put(BibleBook.COR1, new BookRef(46, "1corinthiens", 16));
        BOOKS.put(BibleBook.COR2, new BookRef(47, "2corinthiens", 13));
        BOOKS.put(BibleBook.GAL, new BookRef(48, "galates", 6));
        BOOKS.put(BibleBook.EPH, new BookRef(49, "ephesiens", 6));
        BOOKS.put(BibleBook.PHIL, new BookRef(50, "philippiens", 4));
        BOOKS.put(BibleBook.COL, new BookRef(51, "colossiens", 4));
        BOOKS.put(BibleBook.THESS1, new BookRef(52, "1thessaloniciens", 5));
        BOOKS.put(BibleBook.THESS2, new BookRef(53, "2thessaloniciens", 3));
        BOOKS.put(BibleBook.TIM1, new BookRef(54, "1timothee", 6));
        BOOKS.put(BibleBook.TIM2, new BookRef(55, "2timothee", 4));
        BOOKS.put(BibleBook.TITUS, new BookRef(56, "tite", 3));
        BOOKS.put(BibleBook.PHLM, new BookRef(57, "philemon", 1));
        BOOKS.put(BibleBook.HEB, new BookRef(58, "hebreux", 13));
        BOOKS.put(BibleBook.JAS, new BookRef(59, "jacques", 5));
        BOOKS.put(BibleBook.PET1, new BookRef(60, "1pierre", 5));
        BOOKS.put(BibleBook.PET2, new BookRef(61, "2pierre", 3));
        BOOKS.put(BibleBook.JOHN1, new BookRef(62, "1jean", 5));
        BOOKS.put(BibleBook.JOHN2, new BookRef(63, "2jean", 1));
        BOOKS.put(BibleBook.JOHN3, new BookRef(64, "3jean", 1));
        BOOKS.put(BibleBook.JUDE, new BookRef(65, "jude", 1));
        BOOKS.put(BibleBook.REV, new BookRef(66, "apocalypse", 22));
        BOOKS.put(BibleBook.BAR, new BookRef(67, "baruch", 6));
        BOOKS.put(BibleBook.TOB, new BookRef(68, "tobie", 14));
        BOOKS.put(BibleBook.JDT, new BookRef(69, "judith", 16));
        BOOKS.put(BibleBook.MACC1, new BookRef(70, "1maccabees", 16));
        BOOKS.put(BibleBook.MACC2, new BookRef(71, "2maccabees", 15));
        BOOKS.put(BibleBook.WIS, new BookRef(72, "sagesse", 19));
        BOOKS.put(BibleBook.SIR, new BookRef(73, "ecclesiastique", 51));
    };

    private static class PageParser extends Parser.TerminalParser<Element> {

        private final static Evaluator DOC_TITLE_SELECTOR = QueryParser.parse("h1.mb-3");
        private final static Evaluator MAJOR_SECTION_TITLE_SELECTOR = QueryParser.parse("#logos > h2");
        private final static Evaluator SECTION_TITLE_SELECTOR = QueryParser.parse("#logos > h3:not(:has(> em))");
        private final static Evaluator MINOR_SECTION_TITLE_SELECTOR = QueryParser.parse("#logos > h3:has(> em)");
        private final static Evaluator BOOK_INTRO_PARAGRAPH_SELECTOR = QueryParser.parse("#logos > p");
        private final static Evaluator VERSE_START_SELECTOR = QueryParser.parse("#logos > span.verset");
        private final static Evaluator VERSE_TEXT_SELECTOR = QueryParser.parse("#logos > span[data-verset]");
        private final static Evaluator NOTE_SELECTOR = QueryParser.parse("#logos button.footnote");

        protected PageParser(Stream<Element> docStream, Context rootContext) {
            super(docStream.iterator(), rootContext);
        }

        public static Context parseFlatText(Element e) {
            Context out = new Context(ContextMetadata.forFlatText());
            for(Node n: e.childNodes()) {
                if(n instanceof Element) {
                    if(((Element) n).is(NOTE_SELECTOR)) {
                        addDescendant(out, buildContext(ContextMetadata.forNote(),
                                new Context(ContextMetadata.forText(), n.attr("data-bs-content"))
                        ));
                    }
                    else if(((Element) n).is("br")) {
                        addDescendant(out, new Context(ContextMetadata.forText(), " "));
                    }
                    else {
                        addDescendant(out, new Context(ContextMetadata.forText(), ((Element) n).text()));
                    }
                }
                else if(n instanceof TextNode) {
                    addDescendant(out, new Context(ContextMetadata.forText(), ((TextNode) n).text()));
                }
            }
            return out;
        }

        private boolean isMajorSectionTitle(Element e) {
            // H2 can be either major or normal... See https://theo.place/bible-dby-7-3
            // It is major only if immediately followed by a normal.
            return e.is(MAJOR_SECTION_TITLE_SELECTOR) && e.nextElementSibling().is(SECTION_TITLE_SELECTOR);
        }
        private boolean isSectionTitle(Element e) {
            // If H2 is not major, then it's a normal section.
            return e.is(SECTION_TITLE_SELECTOR) || (e.is(MAJOR_SECTION_TITLE_SELECTOR) && !isMajorSectionTitle(e));
        }

        @Override
        protected Context readContext(Deque<ContextMetadata> ancestors, ContextType type,
                                      ContextMetadata previousOfType, Element e) {
            ContextMetadata parent = ancestors.peekFirst();

            switch(type) {
                case BOOK_TITLE:
                    return e.is(DOC_TITLE_SELECTOR) ? buildContext(
                            ContextMetadata.forBookTitle(parent.book),
                            new Context(ContextMetadata.forText(), e.ownText())
                    ) : null;

                /*
                case CHAPTER_TITLE:
                    return e.is(H1_SELECTOR) ? buildDeepContext(
                            ContextMetadata.forChapterTitle(parent.book, parent.chapter), null,
                            new Context(
                                    ContextMetadata.forText(),
                                    e.ownText()
                            )
                    ) : null;
                */

                case VERSE:
                    return e.is(VERSE_START_SELECTOR) ? new Context(
                            ParsingUtils.getVerseMetadata(parent, previousOfType, e.text()), e.text()
                    ) : null;

                case MAJOR_SECTION_TITLE:
                    return isMajorSectionTitle(e) ? buildContext(
                            ContextMetadata.forMajorSectionTitle(),
                            new Context(ContextMetadata.forText(), e.text())
                    ) : null;

                case SECTION_TITLE:
                    return isSectionTitle(e) ? buildContext(
                            ContextMetadata.forSectionTitle(),
                            new Context(ContextMetadata.forText(), e.text())
                    ) : null;

                case MINOR_SECTION_TITLE:
                    return e.is(MINOR_SECTION_TITLE_SELECTOR) ? buildContext(
                            ContextMetadata.forMinorSectionTitle(),
                            new Context(ContextMetadata.forText(), e.text())
                    ) : null;

                case FLAT_TEXT:
                    if(hasAncestor(ContextType.BOOK_INTRO, ancestors)) {
                        return e.is(BOOK_INTRO_PARAGRAPH_SELECTOR) ? parseFlatText(e) : null;
                    }
                    if(hasAncestor(ContextType.VERSE, ancestors)) {
                        return e.is(VERSE_TEXT_SELECTOR) ? parseFlatText(e) : null;
                    }
                    return null;
            }

            return null;
        }
    }

    private final CachedDownloader downloader;
    private final String bible;

    public TheoPlace(Path cachePath, String[] inputs) throws IOException {
        this.bible = inputs[0];
        this.downloader = new CachedDownloader(cachePath.resolve("TheoPlace").resolve(bible));
    }

    private ContextStream.Single getContextStream(final CachedDownloader downloader, final String url,
                                                  final Context rootContext) {
        // We can't just call the parser's asContextStream method here, because we first need to check if the
        // context exists for the specific bible requested. If not, we don't throw an error, just return an empty
        // context stream.
        return new ContextStream.Single(rootContext.metadata, StreamUtils.deferredStream(() -> {
            try {
                Document doc = Jsoup.parse(downloader.getFile(new URL(url)).toFile());
                if(!doc.select("h3:contains(Livre ou chapitre inexistant)").isEmpty()) {
                    return Stream.of();
                }
                return new PageParser(doc.stream(), rootContext).asContextStream().getStream();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public ContextStream.Single getContextStreamFor(ContextMetadata rootContextMeta) {
        BookRef bookRef;
        switch(rootContextMeta.type) {
            case CHAPTER:
                bookRef = BOOKS.get(rootContextMeta.book);
                Context chapterCtx = new Context(rootContextMeta, Integer.toString(rootContextMeta.chapter));
                return getContextStream(downloader, bookRef.getChapterUrl(bible, rootContextMeta.chapter), chapterCtx);

            case BOOK:
                bookRef = BOOKS.get(rootContextMeta.book);
                Context bookCtx = new Context(rootContextMeta);

                ContextStream.Single bookStream = getContextStream(downloader, bookRef.getBookIntroUrl(bible), bookCtx);
                List<ContextStream.Single> chapterStreams = new ArrayList<>();
                for(int i = 1; i <= bookRef.nbChapters; i++) {
                    ContextStream.Single cs = getContextStreamFor(ContextMetadata.forChapter(rootContextMeta.book, i));
                    if(cs != null) {
                        chapterStreams.add(cs);
                    }
                }
                return bookStream.edit().inject(
                        ContextStreamEditor.InjectionPosition.AT_END, rootContextMeta, chapterStreams
                ).process();

            case BIBLE:
                return autoGetBibleStream(new ArrayList<>(BOOKS.keySet()));
        }
        return null;
    }
}
