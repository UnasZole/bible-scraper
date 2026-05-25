package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.JarUtils;
import com.github.unaszole.bible.datamodel.valuetypes.StdValueTypes;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.monitor.ExecutionMonitor;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.scraping.generic.Config;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.ValueTypeMapper;
import com.github.unaszole.bible.scraping.generic.data.*;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import com.github.unaszole.bible.scraping.generic.index.IndexedContentParser;
import com.github.unaszole.bible.scraping.generic.index.NotifyingIterator;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.downloading.CachedDownloader;
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

    private ContextStream.Single contextStreamer(Context ctx, List<PageData> pages) {
        ValueMapper mapper = new ValueMapper()
                .withMapper(StdValueTypes.BIBLE_BOOK, new ValueTypeMapper.BibleBookMapper(config.bible.getBookReferences()));

        final ExecutionMonitor.Item statusItem = ExecutionMonitor.INSTANCE.register(ctx.metadata.id.toString());
        return new Parser.TerminalParser<>(
                new IndexedContentParser(
                        config.parsers.stream()
                                .collect(Collectors.toMap(
                                        p -> Optional.ofNullable(p.id).orElse("main"),
                                        p -> p
                                )),
                        downloader, mapper
                ),
                new NotifyingIterator<>(pages.stream()
                        .map(IndexNode::new).iterator(), statusItem::start, statusItem::complete),
                ctx
        ).asContextStream();
    }

    private PatternConfig globalDefaults() {
        return config.getGlobalDefaults(flagValues);
    }

    @Override
    public DocumentMetadata getMeta() {
        return config.bible.getDocMeta(globalDefaults());
    }

    @Override
    protected ContextStream.Single getContextStreamFor(final ContextMetadata rootContextMeta) {
        ValueMapper mapper = new ValueMapper()
                .withMapper(StdValueTypes.BIBLE_BOOK, new ValueTypeMapper.BibleBookMapper(config.bible.getBookReferences()));

        IndexNode rootNode = config.bible.getRootNode(globalDefaults(), mapper);

        IndexNode closestAncestor = rootNode.find(rootContextMeta)
                .orElseThrow(() -> new IllegalArgumentException("Could not find any entry for " + rootContextMeta + " in the index."));

        Map<String, TextParser> namedParsers = config.parsers.stream()
                .collect(Collectors.toMap(
                        p -> Optional.ofNullable(p.id).orElse("main"),
                        p -> p
                ));

        ContextStream.Single ctxStream = closestAncestor.stream(namedParsers, downloader, mapper).extractStream(rootContextMeta);

        // Configure editor if provided and we have a bible to return.
        if(config.bible.edit != null) {
            ContextStreamEditor<ContextStream.Single> editor = ctxStream.edit();
            for(StreamEditorConfig cfg: config.bible.edit) {
                cfg.configureEditor(editor, null, 0);
            }
            ctxStream = editor.process();
        }

        return ctxStream;
    }

}
