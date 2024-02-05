package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.scraping.Scraper;
import picocli.CommandLine;

import java.nio.file.Path;

public class ScraperArgument {

    @CommandLine.Option(names = {"--scraper", "-s"}, required = true, description = "Class name (or FQN) of the scraper to use.")
    private String className;

    @CommandLine.Option(names = {"--scraperFlags", "-f"}, description = "Options to pass to the scraper.")
    private String[] flags = {};

    public Scraper get(Path cachePath) throws Exception {
        Class<? extends Scraper> scraperClass = (Class<? extends Scraper>) Class.forName(className.contains(".")
                ? className
                : "com.github.unaszole.bible.scraping.implementations." + className);

        return scraperClass.getConstructor(Path.class, String[].class).newInstance(cachePath, flags);
    }
}
