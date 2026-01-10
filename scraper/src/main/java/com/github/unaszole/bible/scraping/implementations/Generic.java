package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.JarUtils;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.monitor.ExecutionMonitor;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.Config;
import com.github.unaszole.bible.scraping.generic.data.*;
import com.github.unaszole.bible.scraping.generic.parsing.PageListParser;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.downloading.HttpSourceFile;
import com.github.unaszole.bible.scraping.Scraper;
import com.github.unaszole.bible.stream.ContextStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Generic extends Scraper {

    private static final Logger LOG = LoggerFactory.getLogger(Generic.class);

    public static Help getHelp(String[] inputs) throws Exception {
        Config config = null;
        if(inputs.length >= 1) {
            try {
                config = getConfig(inputs[0]);
            } catch (IOException ignored) {
            }
        }

        String title = "Generic scraper";
        if(config == null) {
            return new Help(title, List.of(
                    Map.entry("config (required)",
                            "Path to a YAML file configuring the generic scraper, or one of " +
                                    JarUtils.listResources("scrapers/Generic")
                                            .map(Path::getFileName)
                                            .map(Path::toString)
                                            .map(s -> s.substring(0, s.lastIndexOf(".")))
                                            .collect(Collectors.toList())
                    )
            ), false);
        }

        boolean validInputs = inputs.length == 1;
        List<Map.Entry<String, String>> options = new ArrayList<>();
        options.add(Map.entry("config (required)", inputs[0]));
        if(config.inputs != null) {
            for (String inputName : config.inputs) {
                options.add(Map.entry(inputName + " (required)", ""));
            }
            validInputs = inputs.length == config.inputs.size() + 1;
        }
        return new Help(title + " : " + (config.description != null ? config.description : inputs[0]),
                options, validInputs);

    }

    private static Config getConfig(String flag) throws IOException {
        // If the flag is the path to an existing file, use it.
        File configFile = new File(flag);
        if(configFile.exists()) {
            return Config.parse(Files.newInputStream(configFile.toPath()));
        }

        // Else, try to load one from the embedded resources.
        try(InputStream is = Generic.class.getResourceAsStream("/scrapers/Generic/" + flag + ".yaml")) {
            return Config.parse(is);
        }
    }

    private static Path getCacheSubPath(Path cachePath, String[] flags) {
        Path outPath = cachePath.resolve("Generic");

        File configFile = new File(flags[0]);
        if(configFile.exists()) {
            outPath = outPath.resolve(configFile.getName());
        }
        else {
            outPath = outPath.resolve(flags[0]);
        }

        for(int i = 1; i < flags.length; i++) {
            outPath = outPath.resolve(flags[i]);
        }

        return outPath;
    }

    private final CachedDownloader downloader;
    private final Config config;
    private final List<String> flagValues;

    public Generic(Path cachePath, String[] inputs) throws IOException {
        this.flagValues = Arrays.stream(inputs).skip(1).collect(Collectors.toList());
        this.config = getConfig(inputs[0]);
        this.downloader = new CachedDownloader(getCacheSubPath(cachePath, inputs));
    }

    private static class NotifyingIterator<T> implements Iterator<T> {

        private final Iterator<T> wrapped;
        private final Runnable startCallback;
        private boolean startNotified = false;
        private final Runnable endCallback;
        private boolean endNotified = false;

        private NotifyingIterator(Iterator<T> wrapped, Runnable startCallback, Runnable endCallback) {
            this.wrapped = wrapped;
            this.startCallback = startCallback;
            this.endCallback = endCallback;
        }

        @Override
        public boolean hasNext() {
            if(!startNotified) {
                startNotified = true;
                startCallback.run();
            }
            boolean res = wrapped.hasNext();
            if(!res && !endNotified) {
                endNotified = true;
                endCallback.run();
            }
            return res;
        }

        @Override
        public T next() {
            return wrapped.next();
        }
    }

    private ContextStream.Single contextStreamer(Context ctx, List<PageData> pages) {
        final ExecutionMonitor.Item statusItem = ExecutionMonitor.INSTANCE.register(ctx.metadata.id.toString());
        return new Parser.TerminalParser<>(
                new PageListParser(
                        config.parsers.stream()
                                .collect(Collectors.toMap(
                                        p -> Optional.ofNullable(p.id).orElse("main"),
                                        p -> p
                                )),
                        downloader, config.bible.getBookReferences()
                ),
                new NotifyingIterator<>(pages.iterator(), statusItem::start, statusItem::complete),
                ctx
        ).asContextStream();
    }

    private PatternContainer globalDefaults() {
        return config.getGlobalDefaults(flagValues);
    }

    @Override
    public DocumentMetadata getMeta() {
        return config.bible.getDocMeta(globalDefaults());
    }

    @Override
    protected ContextStream.Single getContextStreamFor(final ContextMetadata rootContextMeta) {
        Book book;
        ChapterSeq seq;
        switch ((BibleContainers) rootContextMeta.type) {
            case CHAPTER:
                // Fetch book and chapter sequence. If we can't find them, nothing to load, return null.
                book = config.bible.getBook(rootContextMeta.id.get(BibleIdFields.BOOK));
                if(book == null) {
                    return null;
                }
                seq = book.getChapterSeq(rootContextMeta.id.get(BibleIdFields.CHAPTER));
                if(seq == null) {
                    return null;
                }

                // Stream the requested chapter.
                return seq.streamChapter(book.defaultedBy(config.bible.defaultedBy(globalDefaults())), rootContextMeta,
                        new HttpSourceFile.Builder(), this::contextStreamer);

            case BOOK:
                // Fetch book. If we can't find it, nothing to load, return null.
                book = config.bible.getBook(rootContextMeta.id.get(BibleIdFields.BOOK));
                if(book == null) {
                    return null;
                }

                // Stream the requested book.
                return book.streamBook(config.bible.defaultedBy(globalDefaults()), rootContextMeta,
                        new HttpSourceFile.Builder(), this::contextStreamer);

            case BIBLE:
                return config.bible.streamBible(globalDefaults(), rootContextMeta,
                        new HttpSourceFile.Builder(), this::contextStreamer);
        }

        return null;
    }

}
