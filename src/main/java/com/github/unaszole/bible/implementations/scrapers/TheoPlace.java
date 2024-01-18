package com.github.unaszole.bible.implementations.scrapers;

import com.github.unaszole.bible.CachedDownloader;
import com.github.unaszole.bible.osisbuilder.parser.*;
import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class TheoPlace implements Scraper {

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

        public Document getBookIntroDocument(CachedDownloader downloader, String bible) {
            String url = BOOK_INTRO_URL
                    .replace("{BIBLE}", bible)
                    .replace("{BOOK_NB}", Integer.toString(bookNb))
                    .replace("{BOOK_NAME}", bookName);

            try {
                return Jsoup.parse(downloader.getFile(new URL(url)).toFile());
            } catch (IOException e) {
                // TODO log
                return null;
            }
        }

        public Document getChapterDocument(CachedDownloader downloader, String bible, int chapterNb) {
            String url = CHAPTER_URL
                    .replace("{BIBLE}", bible)
                    .replace("{BOOK_NB}", Integer.toString(bookNb))
                    .replace("{CHAPTER_NB}", Integer.toString(chapterNb));

            try {
                return Jsoup.parse(downloader.getFile(new URL(url)).toFile());
            } catch (IOException e) {
                // TODO log
                return null;
            }
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
        BOOKS.put(BibleBook.JOEL, new BookRef(29, "joël", 3));
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

    private static class ChapterParser extends Parser<Element> {

        private final static Evaluator CHAPTER_TITLE_SELECTOR = QueryParser.parse("h1.mb-3");
        private final static Evaluator SECTION_TITLE_SELECTOR = QueryParser.parse("#logos > h2, #logos > h3");
        private final static Evaluator VERSE_START_SELECTOR = QueryParser.parse("#logos > span.verset");
        private final static Evaluator VERSE_TEXT_SELECTOR = QueryParser.parse("#logos > span[data-verset]");

        @Override
        protected Context readContext(Deque<ContextMetadata> ancestors, ContextType type, Element e) {
            ContextMetadata parent = ancestors.peekFirst();

            switch(type) {
                case CHAPTER_TITLE:
                    return e.is(CHAPTER_TITLE_SELECTOR) ? new Context(
                            ContextMetadata.forChapterTitle(parent.book, parent.chapter),
                            new Context(
                                    ContextMetadata.forText(),
                                    e.ownText()
                            )
                    ) : null;

                case VERSE:
                    return e.is(VERSE_START_SELECTOR) ? new Context(
                            ContextMetadata.forVerse(parent.book, parent.chapter, Integer.valueOf(e.text()))
                    ) : null;

                case TEXT:
                    if(ancestors.stream().anyMatch(a -> a.type == ContextType.SECTION_TITLE)) {
                        return e.is(SECTION_TITLE_SELECTOR) ? new Context(
                                ContextMetadata.forText(),
                                e.text()
                        ) : null;
                    }

                    if(ancestors.stream().anyMatch(a -> a.type == ContextType.VERSE)) {
                        return e.is(VERSE_TEXT_SELECTOR) ? new Context(
                                ContextMetadata.forText(),
                                e.text()
                        ) : null;
                    }
            }

            return null;
        }
    }

    private final CachedDownloader downloader;
    private final String bible;

    public TheoPlace(Path cachePath, String[] flags) throws IOException {
        this.bible = flags[0];
        this.downloader = new CachedDownloader(cachePath.resolve("TheoPlace").resolve(bible));
    }

    @Override
    public Context fetch(ContextMetadata wantedContext) {
        if(wantedContext.chapter != 0) {
            // If fetching a specific chapter, parse from the corresponding document.
            BookRef book = BOOKS.get(wantedContext.book);

            Document doc = book.getChapterDocument(downloader, bible, wantedContext.chapter);
            if(!doc.select("h3:contains(Livre ou chapitre inexistant)").isEmpty()) {
                return null;
            }

            Context chapterCtx = new Context(ContextMetadata.forChapter(wantedContext.book, wantedContext.chapter));
            return new ChapterParser().extract(
                    doc.stream(),
                    chapterCtx,
                    null,
                    wantedContext
            );
        }
        if(wantedContext.book != null) {
            BookRef book = BOOKS.get(wantedContext.book);
            if(book == null) {
                return null;
            }

            if(wantedContext.type == ContextType.BOOK_TITLE) {
                Element title = book.getBookIntroDocument(downloader, bible).select("h1").first();
                if(title != null) {
                    return new Context(wantedContext,
                            new Context(ContextMetadata.forText(), title.ownText()));
                }
                return null;
            }
            if(wantedContext.type == ContextType.BOOK_INTRO) {
                Elements paragraphs = book.getBookIntroDocument(downloader, bible).select("#logos > p");
                if(!paragraphs.isEmpty()) {
                    Context introCtx = new Context(wantedContext);
                    Context structuredTextCtx = new Context(ContextMetadata.forStructuredText());
                    introCtx.addChild(structuredTextCtx);

                    for(Element paragraph: paragraphs) {
                        structuredTextCtx.addChild(new Context(ContextMetadata.forText(), paragraph.text()));
                        structuredTextCtx.addChild(new Context(ContextMetadata.forParagraphBreak()));
                    }

                    return introCtx;
                }
                return null;
            }
            if(wantedContext.type == ContextType.BOOK) {
                // If fetching a full book, build from all subcomponents.
                Context bookCtx = new Context(wantedContext);
                Context titleCtx = fetch(ContextMetadata.forBookTitle(wantedContext.book));
                if(titleCtx != null) {
                    bookCtx.addChild(titleCtx);
                }
                Context introCtx = fetch(ContextMetadata.forBookIntro(wantedContext.book));
                if(introCtx != null) {
                    bookCtx.addChild(introCtx);
                }
                for(int i = 1; i <= BOOKS.get(wantedContext.book).nbChapters; i++) {
                    Context chapterCtx = fetch(ContextMetadata.forChapter(wantedContext.book, i));
                    if(chapterCtx != null) {
                        bookCtx.addChild(chapterCtx);
                    }
                }

                return bookCtx.getChildren().isEmpty() ? null : bookCtx;
            }
        }
        if(wantedContext.type == ContextType.BIBLE) {
            Context bibleCtx = new Context(ContextMetadata.forBible());
            for(BibleBook book: BOOKS.keySet()) {
                Context bookCtx = fetch(ContextMetadata.forBook(book));
                if(bookCtx != null) {
                    bibleCtx.addChild(bookCtx);
                }
            }
            return bibleCtx.getChildren().isEmpty() ? null : bibleCtx;
        }
        return null;
    }
}
