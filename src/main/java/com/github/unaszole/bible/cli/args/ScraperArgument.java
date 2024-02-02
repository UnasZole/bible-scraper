package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.scraping.Scraper;
import picocli.CommandLine;

import java.nio.file.Path;

public class ScraperArgument {

    @CommandLine.Option(names = {"--scraper", "-s"}, required = true)
    private String className;

    @CommandLine.Option(names = {"--scraperFlags", "-f"})
    private String[] flags = {};

    public Scraper get(Path cachePath) throws Exception {
        Class<? extends Scraper> scraperClass = (Class<? extends Scraper>) Class.forName(className.contains(".")
                ? className
                : "com.github.unaszole.bible.scraping.implementations." + className);

        return scraperClass.getConstructor(Path.class, String[].class).newInstance(cachePath, flags);
    }
}
