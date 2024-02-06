package com.github.unaszole.bible.scraping.implementations;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericHtml extends Scraper {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()).registerModule(new SimpleModule()
            .addDeserializer(Evaluator.class, new StdDeserializer<>(Evaluator.class) {
                @Override
                public Evaluator deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return QueryParser.parse(jsonParser.readValueAs(String.class));
                }
            })
            .addDeserializer(BibleBook.class, new StdDeserializer<>(BibleBook.class) {
                @Override
                public BibleBook deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return BibleBook.fromOSIS(jsonParser.readValueAs(String.class));
                }
            })
    );

    public interface Accessor<T> extends Function<Function<T, String>, String> {
        default Accessor<T> overriddenBy(final T overrides) {
            return overrides == null ? this : (
                    getter -> Optional.ofNullable(getter.apply(overrides)).orElse(this.apply(getter))
            );
        }

        default List<Accessor<T>> overriddenByList(final List<T> overrides) {
            return overrides.stream().map(this::overriddenBy).collect(Collectors.toList());
        }
    }

    /**
     * A set of variables to locate and identify source documents.
     *
     * All these variables may be defined globally, at book level, or at "chapter sequence" level.
     * The definition at "chapter sequence" level, if present, overrides the one at book level, which in turn overrides
     * the global.
     *
     * All these variables may contain arguments in the form {ARGUMENT_NAME}, that will be substituted with argument
     * values provided for each book or chapter.
     *
     * Thus, while the recommended strategy is to set these variables globally with a pattern, and use arguments to
     * customise the value for each book and chapter, it's also possible to override them entirely for specific cases.
     */
    private static class SourceVars {

        /**
         * The URL of the page for a book, or part of a book (either a book introduction, or a sequence of
         * several chapters).
         */
        public String bookUrl;
        /**
         * The URL of the page for a chapter, or part of a chapter.
         * This should be specified if and only if {@link #bookUrl} is unset, empty, or points to a page that does not
         * contain any chapter.
         */
        public String chapterUrl;
        /**
         * The "published number" for a chapter.
         * This is only relevant if {@link #chapterUrl} is provided, and allows customising the number that will
         * actually be displayed for the extracted chapter.
         * Note that if a chapter is loaded from several pages and each page gets a different published number,
         * all of them will be concatenated with "-".
         * If omitted, then the published number will be the OSIS chapter number.
         */
        public String chapterPublishedNumber;

        public Accessor<SourceVars> getAccessor() {
            return (getter -> getter.apply(this));
        }

        @Override
        public String toString() {
            try {
                return MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class StreamEditorConfig {
        public static class Metadata {
            public ContextType type;
            public BibleBook book;
            public Integer chapter;
            public Integer verse;

            public ContextMetadata get(BibleBook defaultBook, int defaultChapter) {
                return new ContextMetadata(type,
                        book != null ? book : defaultBook,
                        chapter != null ? chapter : defaultChapter,
                        verse
                );
            }
        }

        public static class VersificationUpdate {
            public Integer shiftChapter;
            public Integer shiftVerse;

            public ContextStreamEditor.VersificationUpdater getUpdater() {
                ContextStreamEditor.VersificationUpdater updater = new ContextStreamEditor.VersificationUpdater();
                if(shiftChapter != null) {
                    updater.chapterNb(m -> m.chapter + shiftChapter);
                }
                if(shiftVerse != null) {
                    updater.verseNbs(m -> Arrays.stream(m.verses).map(v -> v + shiftVerse).toArray());
                }
                return updater;
            }
        }

        public Metadata from;
        public Metadata to;
        public VersificationUpdate updateVersification;

        public <T extends ContextStream<T>> ContextStreamEditor<T> configureEditor(ContextStreamEditor<T> editor, BibleBook defaultBook, int defaultChapter) {
            ContextMetadata fromMeta = from.get(defaultBook, defaultChapter);
            ContextMetadata toMeta = to.get(defaultBook, defaultChapter);
            if(updateVersification != null) {
                editor.updateVersification(fromMeta, toMeta, updateVersification.getUpdater());
            }
            return editor;
        }
    }

    /**
     * Specifies the contents of a book to retrieve from the source.
     */
    private static class Book {

        /**
         * Specifies a sequence of chapters from this book.
         */
        private static class ChapterSeq {
            /**
             * The OSIS number of this chapter, if this sequence represents a single chapter.
             * If given, then {@link #from} and {@link #to} MUST be omitted.
             */
            public Integer at;
            /**
             * The first OSIS chapter number of this sequence.
             * If given, then {@link #at} MUST be omitted, and {@link #to} MUST be provided.
             */
            public Integer from;
            /**
             * The last OSIS chapter number of this sequence.
             * If given, then {@link #at} MUST be omitted, and {@link #from} MUST be provided.
             */
            public Integer to;
            /**
             * Overrides of the source variables for this chapter sequence.
             */
            public SourceVars sourceVars;
            /**
             * List of pages to retrieve for each chapter in this sequence, each page being specified by a set of
             * arguments.
             * In a chapter sequence only, in addition to direct string values as everywhere else, it's possible to
             * specify arguments as simple integer expressions : these must start by "=" and be of the form
             * "= $i + 1" or "= $i - 2", where $i is the OSIS number of the chapter.
             */
            public List<Map<String, String>> args;
            /**
             * Configuration for a stream editor.
             */
            public List<StreamEditorConfig> edit;

            private static final Pattern CHAPTER_EXPR = Pattern.compile("^=\\s*([^\\s]+)\\s*(([+-])\\s*([^\\s]+)\\s*)?$");
            private int eval(String str, int chapterNb) {
                return str.equals("$i") ? chapterNb : Integer.parseInt(str);
            }

            public Stream<Integer> listChapters() {
                if(at != null) {
                    return Stream.of(at);
                }
                else {
                    return Stream.iterate(from, n -> n <= to, n -> n + 1);
                }
            }

            public boolean containsChapter(int chapterNb) {
                if(at != null) {
                    assert from == null && to == null : this + " invalid : if 'at' is specified, 'from' and 'to' are forbidden.";
                    return at == chapterNb;
                }
                else {
                    assert from != null && to != null : this + " invalid : if 'at' unspecified, 'from' and 'to' are mandatory.";
                    return from <= chapterNb && chapterNb <= to;
                }
            }

            private String evalChapterArg(String arg, int chapterNb) {
                if(!arg.startsWith("=")) {
                    // Argument is not a numeric expression : return it as is.
                    return arg;
                }

                Matcher expr = CHAPTER_EXPR.matcher(arg);
                if(expr.matches()) {
                    int val = eval(expr.group(1), chapterNb);
                    if (expr.group(2) != null) {
                        if ("+".equals(expr.group(3))) {
                            val += eval(expr.group(4), chapterNb);
                        } else {
                            val -= eval(expr.group(4), chapterNb);
                        }
                    }
                    return Integer.toString(val);
                }
                throw new IllegalArgumentException("Invalid expression " + arg);
            }

            public List<Map<String, String>> evaluateChapterArgs(int chapterNb) {
                return args.stream().map(
                        pageArgs -> pageArgs.entrySet().stream()
                                .map(e -> Map.entry(e.getKey(), evalChapterArg(e.getValue(), chapterNb)))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ).collect(Collectors.toList());
            }

            @Override
            public String toString() {
                try {
                    return MAPPER.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * The OSIS ID of the book.
         */
        public BibleBook osis;
        /**
         * Overrides of the source variables for this book.
         */
        public SourceVars sourceVars;
        /**
         * Set of arguments for this book.
         */
        public Map<String, String> args;
        /**
         * Description of this book's contents as a sequence of chapters.
         */
        public List<ChapterSeq> chapters;
        /**
         * Configuration for a stream editor.
         */
        public List<StreamEditorConfig> edit;

        public ChapterSeq getChapterSeq(int chapterNb) {
            for(ChapterSeq seq: chapters) {
                if(seq.containsChapter(chapterNb)) {
                    return seq;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            try {
                return MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Configuration to extract a context from an HTML element.
     */
    public static class ContextExtractor {
        /**
         * Configuration for extracting a String from an HTML element.
         */
        public static class Extractor {
            /**
             * A selector to select a descendant of the current element.
             * If omitted, the operator applies to the current element itself.
             */
            public Evaluator selector;
            /**
             * An operator to extract a string from the selected element. The following are supported :
             * <li>text : extract the full text of this element and all its descendants.</li>
             * <li>ownText : extract the text of this element, excluding its descendants.</li>
             */
            public String op;

            public String extract(Element e) {
                Element target = selector != null ? e.select(selector).first() : e;

                switch(op) {
                    case "text":
                        return target.text();
                    case "ownText":
                        return target.ownText();
                }

                if(op.startsWith("attribute=")) {
                    return target.attr(op.substring("attribute=".length()));
                }

                return null;
            }

            @Override
            public String toString() {
                try {
                    return MAPPER.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * The type of contexts this extractor produces.
         */
        public ContextType type;
        /**
         * A required ancestor type for this extractor to trigger. Only relevant for a root extractor.
         */
        public ContextType withAncestor;
        /**
         * A selector that selects only HTML elements that this extractor can build a context from.
         * For an extractor at the root of the parser, MUST always be provided : checks if the incoming element can open
         * this context.
         * For a descendant extractor, may be omitted, in which case the descendant context will be extracted
         * from the same element.
         */
        public Evaluator selector;
        /**
         * Configuration to extract a value for this context. This extractor is relative to the element selected by the
         * {@link #selector}.
         * This MUST be set for CHAPTER, VERSE and TEXT contexts !
         * It should be left null for other contexts.
         */
        public Extractor valueExtractor;
        /**
         * Configuration to extract additional descendant contexts under this one.
         * All extractors are relative to the element selected by the {@link #selector}.
         */
        public List<ContextExtractor> descendantExtractors;

        private static String stripLeadingZeroes(String parsedNb) {
            return parsedNb.length() == 1 ? parsedNb : parsedNb.replaceAll("^0*(.)", "$1");
        }

        private Context extractInternal(Element targetElt, ContextMetadata parent, ContextMetadata previousOfType) {
            String value = valueExtractor != null ? valueExtractor.extract(targetElt) : null;

            ContextMetadata meta;
            switch (type) {
                case CHAPTER:
                    meta = ContextMetadata.forChapter(parent.book, Integer.parseInt(value));
                    break;
                case VERSE:
                    value = stripLeadingZeroes(value);
                    meta = ParsingUtils.getVerseMetadata(parent, previousOfType, value);
                    break;
                default:
                    meta = new ContextMetadata(type, parent.book, parent.chapter, parent.verses);
            }

            Context[] descendants = new Context[0];
            if (descendantExtractors != null) {
                descendants = new Context[descendantExtractors.size()];

                for (int i = 0; i < descendantExtractors.size(); i++) {
                    ContextExtractor descendant = descendantExtractors.get(i);
                    descendants[i] = descendant.extractDescendantContext(targetElt, meta, null);
                }
            }

            return Parser.buildContext(meta, value, descendants);
        }

        public Context extractDescendantContext(Element parentElt, ContextMetadata parent, ContextMetadata previousOfType) {
            Element targetElt = selector != null ? parentElt.select(selector).first() : parentElt;
            return extractInternal(targetElt, parent, previousOfType);
        }

        public Context extractRootContext(Element e, ContextMetadata parent, ContextMetadata previousOfType) {
            if(e.is(selector)) {
                return extractInternal(e, parent, previousOfType);
            }
            return null;
        }

        @Override
        public String toString() {
            try {
                return MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Config {
        public SourceVars sourceVars;
        public List<String> flags;
        public List<Book> books;
        public List<ContextExtractor> parser;

        public Book getBook(BibleBook book) {
            return books.stream()
                    .filter(b -> b.osis == book)
                    .findAny()
                    .orElse(null);
        }

        public Accessor<Map<String, String>> getArgsAccessor(List<String> flagValues) {
            int nbExpectedFlags = flags == null ? 0 : flags.size();
            int nbGivenFlags = flagValues.size();
            if(nbGivenFlags != nbExpectedFlags) {
                throw new IllegalArgumentException("Provided " + nbGivenFlags
                        + " flags, but expecting " + nbExpectedFlags
                );
            }

            final Map<String, String> rootArgs = new HashMap<>();
            for(int i = 0; i < flagValues.size(); i++) {
                rootArgs.put(flags.get(i), flagValues.get(i));
            }

            return getter -> getter.apply(rootArgs);
        }

        @Override
        public String toString() {
            try {
                return MAPPER.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final CachedDownloader downloader;
    private final Config config;
    private final List<String> flags;

    public GenericHtml(Path cachePath, String[] flags) throws IOException {
        File configFile = new File(flags[0]);
        this.flags = Arrays.stream(flags).skip(1).collect(Collectors.toList());
        this.config = MAPPER.readValue(configFile, Config.class);
        this.downloader = new CachedDownloader(cachePath.resolve("GenericHtml").resolve(configFile.getName()));
    }

    private List<BibleBook> getBooks() {
        return config.books.stream().map(b -> b.osis).collect(Collectors.toList());
    }

    private Document downloadAndParse(final CachedDownloader downloader, String url) {
        try {
            return Jsoup.parse(downloader.getFile(new URL(url)).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<Element> getDocStream(final CachedDownloader downloader, List<String> urls) {
        return StreamUtils.concatStreams(
                urls.stream()
                        .map(pageUrl -> StreamUtils.deferredStream(
                                () -> downloadAndParse(downloader, pageUrl).stream()
                        ))
                        .collect(Collectors.toList())
        );
    }

    private static class ConfiguredHtmlParser extends Parser.TerminalParser<Element> {

        private final List<ContextExtractor> parserConfig;

        private ConfiguredHtmlParser(List<ContextExtractor> parserConfig,
                                     Stream<Element> docStream, Context rootContext) {
            super(docStream.iterator(), rootContext);
            this.parserConfig = parserConfig;
        }

        @Override
        protected Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                                      ContextMetadata previousOfType, Element e) {
            ContextExtractor extractor = parserConfig.stream()
                    .filter(ex -> ex.type == type && (ex.withAncestor == null
                            || ancestorStack.stream().anyMatch(a -> a.type == ex.withAncestor)))
                    .findFirst()
                    .orElse(null);
            if(extractor != null) {
                return extractor.extractRootContext(e, ancestorStack.peekFirst(), previousOfType);
            }
            return null;
        }
    }

    private static final Pattern ARG_REFERENCE = Pattern.compile("\\{([A-Z0-9_]+)}");
    private String substituteArgs(String str, final Accessor<Map<String, String>> args) {
        Matcher argRefs = ARG_REFERENCE.matcher(str);
        return argRefs.replaceAll(r -> args.apply(m -> m.get(r.group(1))));
    }

    @Override
    protected ContextStream.Single getContextStreamFor(final ContextMetadata rootContextMeta) {
        Book book;
        Book.ChapterSeq seq;
        Accessor<SourceVars> sourceVars;
        switch (rootContextMeta.type) {
            case CHAPTER:
                // Fetch book and chapter sequence to access the relevant source variables and args.
                // If we can't find them, nothing to load, return null.
                book = config.getBook(rootContextMeta.book);
                if(book == null) {
                    return null;
                }
                seq = book.getChapterSeq(rootContextMeta.chapter);
                if(seq == null) {
                    return null;
                }

                // If the requested chapter is explicitly listed in the book structure, load the config.
                sourceVars = config.sourceVars.getAccessor()
                        .overriddenBy(book.sourceVars)
                        .overriddenBy(seq.sourceVars);
                List<Accessor<Map<String, String>>> listArgs = config.getArgsAccessor(flags)
                        .overriddenBy(book.args)
                        .overriddenByList(seq.evaluateChapterArgs(rootContextMeta.chapter));

                if(sourceVars.apply(s -> s.chapterUrl) != null) {
                    // If we have a chapter URL pattern, then we proceed.

                    // Compute chapter value
                    String chapterValue = listArgs.stream()
                            .map(pageArgs -> substituteArgs(sourceVars.apply(v -> v.chapterPublishedNumber), pageArgs))
                            .distinct()
                            .collect(Collectors.joining("-"));

                    // Compute chapter page URLs
                    List<String> chapterUrls = listArgs.stream()
                            .map(pageArgs -> substituteArgs(sourceVars.apply(v -> v.chapterUrl), pageArgs))
                            .collect(Collectors.toList());

                    // Prepare parser
                    Context chapterCtx = new Context(rootContextMeta, chapterValue);
                    ContextStream.Single chapterStream = new ConfiguredHtmlParser(config.parser, getDocStream(downloader, chapterUrls),
                            chapterCtx).asContextStream();

                    // Configure editor if provided.
                    if(seq.edit != null) {
                        ContextStreamEditor<ContextStream.Single> editor = chapterStream.edit();
                        for(StreamEditorConfig cfg: seq.edit) {
                            cfg.configureEditor(editor, rootContextMeta.book, rootContextMeta.chapter);
                        }
                        chapterStream = editor.process();
                    }
                    return chapterStream;
                }

                // No page to retrieve for the chapter : no context to return.
                return null;

            case BOOK:
                // Fetch book to access relevant source variables and args.
                book = config.getBook(rootContextMeta.book);
                if(book == null) {
                    return null;
                }
                sourceVars = config.sourceVars.getAccessor()
                        .overriddenBy(book.sourceVars);
                Accessor<Map<String, String>> args = config.getArgsAccessor(flags).overriddenBy(book.args);

                // Build the list of context streams for all chapters, to append to the book page stream.
                List<ContextStream.Single> chapterStreams;
                if(book.chapters != null && book.chapters.size() > 0) {
                    // If we have a chapter structure defined, build context streams for each.
                    chapterStreams = book.chapters.stream()
                            .flatMap(Book.ChapterSeq::listChapters)
                            .map(chapterNb -> getContextStreamFor(ContextMetadata.forChapter(book.osis, chapterNb)))
                            .collect(Collectors.toList());
                }
                else {
                    // No information on chapters, nothing to append to the book page stream.
                    chapterStreams = List.of();
                }

                // Build the book stream.
                Context bookCtx = new Context(rootContextMeta);
                ContextStream.Single bookStream = null;
                if(sourceVars.apply(s -> s.bookUrl) != null) {
                    // If we have a book URL pattern, we parse it, and then append the chapters.
                    String bookUrl = substituteArgs(sourceVars.apply(s -> s.bookUrl), args);
                    bookStream = new ConfiguredHtmlParser(config.parser,
                            getDocStream(downloader, List.of(bookUrl)), bookCtx
                    ).asContextStream().edit().inject(
                            ContextStreamEditor.InjectionPosition.AT_END, rootContextMeta, chapterStreams
                    ).process();
                }
                else if(!chapterStreams.isEmpty()) {
                    // If we don't have a book page but we have chapter contents, just aggregate them.
                    bookStream = ContextStream.fromContents(bookCtx, chapterStreams);
                }

                // Configure editor if provided and we have a book to return.
                if(bookStream != null && book.edit != null) {
                    ContextStreamEditor<ContextStream.Single> editor = bookStream.edit();
                    for(StreamEditorConfig cfg: book.edit) {
                        cfg.configureEditor(editor, rootContextMeta.book, 0);
                    }
                    bookStream = editor.process();
                }

                return bookStream;

            case BIBLE:
                return autoGetBibleStream(getBooks());
        }

        return null;
    }
}
