package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.*;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.StreamUtils;
import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.unaszole.bible.scraping.ContextReaderListBuilder.context;

public class Aelf extends Scraper {

    public static Help getHelp(String[] inputs) {
        return new Help("Nouvelle traduction liturgique francophone (aelf.org)",
                Collections.emptyList(), inputs.length == 0);
    }

    private static final String CHAPTER_URL="https://www.aelf.org/bible/{BOOK}/{CHAPTER}";

    private static class ChapterRange {
        public final int begin;
        public final int end;

        private final int rangeMappingStart;
        private final String[] singleMappingPages;

        public ChapterRange(int begin, int end, int rangeMappingStart) {
            this.begin = begin;
            this.end = end;
            this.rangeMappingStart = rangeMappingStart;
            this.singleMappingPages = null;
        }

        public ChapterRange(int begin, int end) {
            this.begin = begin;
            this.end = end;
            this.rangeMappingStart = begin;
            this.singleMappingPages = null;
        }

        public ChapterRange(int chapter, String... singleMappingPages) {
            this.begin = chapter;
            this.end = chapter;
            this.singleMappingPages = singleMappingPages;
            this.rangeMappingStart = -1;
        }

        public boolean contains(int chapterNb) {
            return begin <= chapterNb && chapterNb <= end;
        }

        public String[] getPages(int chapterNb) {
            assert contains(chapterNb);

            if(singleMappingPages != null) {
                return singleMappingPages;
            }
            else {
                return new String[] { Integer.toString(rangeMappingStart + (chapterNb - begin) ) };
            }
        }
    }

    private static class BookRef {
        private final String book;
        public final ChapterRange[] ranges;

        public BookRef(String book, ChapterRange... ranges) {
            this.book = book;
            this.ranges = ranges;
        }

        public BookRef(String book, int nbChapters) {
            this(book, new ChapterRange(1, nbChapters, 1));
        }

        public int getNbChapters() {
            int maxChapter = 0;
            for(ChapterRange range: ranges) {
                maxChapter = Math.max(maxChapter, range.end);
            }
            return maxChapter;
        }

        public String[] getPages(int chapterNb) {
            for(ChapterRange range: ranges) {
                if(range.contains(chapterNb)) {
                    return range.getPages(chapterNb);
                }
            }
            return new String[]{};
        }

        private Document getChapterDocument(CachedDownloader downloader, String chapter) {
            String url = CHAPTER_URL
                    .replace("{BOOK}", book)
                    .replace("{CHAPTER}", chapter);

            try {
                return Jsoup.parse(downloader.getFile(new URL(url)).toFile());
            } catch (IOException e) {
                // TODO log
                return null;
            }
        }

        public Stream<Element> getDocStream(final CachedDownloader downloader, int chapterNb) {
            return StreamUtils.concatStreams(
                    Arrays.stream(getPages(chapterNb))
                            .map(page -> StreamUtils.deferredStream(
                                    () -> getChapterDocument(downloader, page).stream()
                            ))
                            .collect(Collectors.toList())
            );
        }
    }

    private static final Map<BibleBook, BookRef> BOOKS = new LinkedHashMap<>();
    static {
        BOOKS.put(BibleBook.GEN, new BookRef("Gn", 50));
        BOOKS.put(BibleBook.EXOD, new BookRef("Ex", 40));
        BOOKS.put(BibleBook.LEV, new BookRef("Lv", 27));
        BOOKS.put(BibleBook.NUM, new BookRef("Nb", 36));
        BOOKS.put(BibleBook.DEUT, new BookRef("Dt", 34));
        BOOKS.put(BibleBook.JOSH, new BookRef("Jos", 24));
        BOOKS.put(BibleBook.JUDG, new BookRef("Jg", 21));
        BOOKS.put(BibleBook.RUTH, new BookRef("Rt", 4));
        BOOKS.put(BibleBook.SAM1, new BookRef("1S", 31));
        BOOKS.put(BibleBook.SAM2, new BookRef("2S", 24));
        BOOKS.put(BibleBook.KGS1, new BookRef("1R", 22));
        BOOKS.put(BibleBook.KGS2, new BookRef("2R", 25));
        BOOKS.put(BibleBook.CHR1, new BookRef("1Ch", 29));
        BOOKS.put(BibleBook.CHR2, new BookRef("2Ch", 36));
        BOOKS.put(BibleBook.EZRA, new BookRef("Esd", 10));
        BOOKS.put(BibleBook.NEH, new BookRef("Ne", 13));
        BOOKS.put(BibleBook.TOB, new BookRef("Tb", 14));
        BOOKS.put(BibleBook.JDT, new BookRef("Jdt", 16));
        BOOKS.put(BibleBook.ESTH, new BookRef("Est", new ChapterRange(1, "0", "1"), new ChapterRange(2,10)));
        BOOKS.put(BibleBook.MACC1, new BookRef("1M", 16));
        BOOKS.put(BibleBook.MACC2, new BookRef("2M", 15));
        BOOKS.put(BibleBook.JOB, new BookRef("Jb", 42));
        BOOKS.put(BibleBook.PROV, new BookRef("Pr", 31));
        BOOKS.put(BibleBook.ECCL, new BookRef("Qo", 12));
        BOOKS.put(BibleBook.SONG, new BookRef("Ct", 8));
        BOOKS.put(BibleBook.WIS, new BookRef("Sg", 19));
        BOOKS.put(BibleBook.SIR, new BookRef("Si", new ChapterRange(0, 51)));
        BOOKS.put(BibleBook.ISA, new BookRef("Is", 66));
        BOOKS.put(BibleBook.JER, new BookRef("Jr", 52));
        BOOKS.put(BibleBook.LAM, new BookRef("Lm", 5));
        BOOKS.put(BibleBook.BAR, new BookRef("Ba", 5));
        BOOKS.put(BibleBook.EP_JER, new BookRef("XXX", new ChapterRange(1, "0")));
        BOOKS.put(BibleBook.EZEK, new BookRef("Ez", 48));
        BOOKS.put(BibleBook.DAN, new BookRef("Dn", 14));
        BOOKS.put(BibleBook.HOS, new BookRef("Os", 14));
        BOOKS.put(BibleBook.JOEL, new BookRef("Jl", 4));
        BOOKS.put(BibleBook.AMOS, new BookRef("Am", 9));
        BOOKS.put(BibleBook.OBAD, new BookRef("Ab", new ChapterRange(1, "0")));
        BOOKS.put(BibleBook.JONAH, new BookRef("Jon", 4));
        BOOKS.put(BibleBook.MIC, new BookRef("Mi", 7));
        BOOKS.put(BibleBook.NAH, new BookRef("Na", 3));
        BOOKS.put(BibleBook.HAB, new BookRef("Ha", 3));
        BOOKS.put(BibleBook.ZEPH, new BookRef("So", 3));
        BOOKS.put(BibleBook.HAG, new BookRef("Ag", 2));
        BOOKS.put(BibleBook.ZECH, new BookRef("Za", 14));
        BOOKS.put(BibleBook.MAL, new BookRef("Ml", 3));

        BOOKS.put(BibleBook.PS, new BookRef("Ps",
                new ChapterRange(1, 8),
                new ChapterRange(9, "9A"),
                new ChapterRange(10, "9B"),
                new ChapterRange(11, 113, 10),
                new ChapterRange(114, "113A"),
                new ChapterRange(115, "113B"),
                new ChapterRange(116, "114", "115"),
                new ChapterRange(117, 146, 116),
                new ChapterRange(147, "146", "147"),
                new ChapterRange(148, 150)
        ));

        BOOKS.put(BibleBook.MATT, new BookRef("Mt", 28));
        BOOKS.put(BibleBook.MARK, new BookRef("Mc", 16));
        BOOKS.put(BibleBook.LUKE, new BookRef("Lc", 24));
        BOOKS.put(BibleBook.JOHN, new BookRef("Jn", 21));
        BOOKS.put(BibleBook.ACTS, new BookRef("Ac", 28));
        BOOKS.put(BibleBook.ROM, new BookRef("Rm", 16));
        BOOKS.put(BibleBook.COR1, new BookRef("1Co", 16));
        BOOKS.put(BibleBook.COR2, new BookRef("2Co", 13));
        BOOKS.put(BibleBook.GAL, new BookRef("Ga", 6));
        BOOKS.put(BibleBook.EPH, new BookRef("Ep", 6));
        BOOKS.put(BibleBook.PHIL, new BookRef("Ph", 4));
        BOOKS.put(BibleBook.COL, new BookRef("Col", 4));
        BOOKS.put(BibleBook.THESS1, new BookRef("1Th", 5));
        BOOKS.put(BibleBook.THESS2, new BookRef("2Th", 3));
        BOOKS.put(BibleBook.TIM1, new BookRef("1Tm", 6));
        BOOKS.put(BibleBook.TIM2, new BookRef("2Tm", 4));
        BOOKS.put(BibleBook.TITUS, new BookRef("Tt", 3));
        BOOKS.put(BibleBook.PHLM, new BookRef("Phm", 1));
        BOOKS.put(BibleBook.HEB, new BookRef("He", 13));
        BOOKS.put(BibleBook.JAS, new BookRef("Jc", 5));
        BOOKS.put(BibleBook.PET1, new BookRef("1P", 5));
        BOOKS.put(BibleBook.PET2, new BookRef("2P", 3));
        BOOKS.put(BibleBook.JOHN1, new BookRef("1Jn", 5));
        BOOKS.put(BibleBook.JOHN2, new BookRef("2Jn", 1));
        BOOKS.put(BibleBook.JOHN3, new BookRef("3Jn", 1));
        BOOKS.put(BibleBook.JUDE, new BookRef("Jude", 1));
        BOOKS.put(BibleBook.REV, new BookRef("Ap", 22));
    }

    private static class PageParser extends PositionBufferedParserCore<Element> {

        private static final String S_VERSE_NB = "span:is(.verse_number, .text-danger)";
        private static final Evaluator VERSE_SELECTOR = QueryParser.parse("#right-col p:has(> " + S_VERSE_NB + ")");
        private static final Evaluator VERSE_NB_SELECTOR = QueryParser.parse("> " + S_VERSE_NB);

        private static String stripLeadingZeroes(String parsedNb) {
            return parsedNb.length() == 1 ? parsedNb : parsedNb.replaceAll("^0*", "");
        }

        private ContextMetadata fixVerseMeta(BibleBook book, int chapter, ContextMetadata verseMeta) {
            if(book == BibleBook.ESTH && chapter == 1) {
                return ContextMetadata.forVerse(book, chapter,
                        verseMeta.verses[0] == 12 ? verseMeta.verses[0] + 7 : verseMeta.verses[0]);
            }
            return verseMeta;
        }

        @Override
        public List<ContextReader> readContexts(Deque<ContextMetadata> ancestors, ContextType type,
                                               ContextMetadata previousOfType, Element e) {
            ContextMetadata parent = ancestors.peekFirst();
            switch(type) {
                case VERSE:
                    if(e.is(VERSE_SELECTOR)) {
                        String verseNb = stripLeadingZeroes(e.selectFirst(VERSE_NB_SELECTOR).text());
                        ContextMetadata verseMeta = fixVerseMeta(parent.book, parent.chapter,
                                ParsingUtils.getVerseMetadata(parent, previousOfType, verseNb));

                        String verseText = e.ownText();

                        return context(verseMeta, verseNb,
                                context(ContextMetadata.forFlatText(),
                                        context(ContextMetadata.forText(), verseText)
                                )
                        ).build();
                    }
            }

            return List.of();
        }
    }

    private final CachedDownloader downloader;

    public Aelf(Path cachePath, String[] inputs) throws IOException {
        this.downloader = new CachedDownloader(cachePath.resolve("Aelf"));
    }

    @Override
    public ContextStream.Single getContextStreamFor(ContextMetadata rootContextMeta) {
        BookRef bookRef;
        Stream<Element> docStream;
        switch (rootContextMeta.type) {
            case CHAPTER:
                bookRef = BOOKS.get(rootContextMeta.book);
                docStream = bookRef.getDocStream(downloader, rootContextMeta.chapter);
                Context chapterCtx = new Context(rootContextMeta,
                        String.join("-", bookRef.getPages(rootContextMeta.chapter))
                );
                return new Parser.TerminalParser<Element>(new PageParser(), docStream.iterator(), chapterCtx)
                        .asContextStream();
            case BOOK:
                return autoGetBookStream(rootContextMeta.book, BOOKS.get(rootContextMeta.book).getNbChapters());
            case BIBLE:
                return autoGetBibleStream(new ArrayList<>(BOOKS.keySet()));
        }
        return null;
    }
}
