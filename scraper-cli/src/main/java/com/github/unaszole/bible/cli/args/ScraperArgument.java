package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.scraping.Scraper;
import picocli.CommandLine;

import java.nio.file.Path;

public class ScraperArgument {

    @CommandLine.Option(names = {"--scraper", "-s"}, required = true, description = "Class name (or FQN) of the scraper to use.")
    private String className;

    @CommandLine.Option(names = {"--input", "-i"}, description = "Inputs to pass to the scraper.")
    private String[] inputs = {};

    private Class<? extends Scraper> getScraperClass() throws ClassNotFoundException {
        return (Class<? extends Scraper>) Class.forName(className.contains(".")
                ? className
                : "com.github.unaszole.bible.scraping.implementations." + className);
    }

    public Scraper.Help getHelp() throws Exception {
        try {
            return (Scraper.Help) getScraperClass()
                    .getMethod("getHelp", String[].class).invoke(null, (Object) inputs);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    public Scraper get(Path cachePath) throws Exception {
        return getScraperClass().getConstructor(Path.class, String[].class).newInstance(cachePath, inputs);
    }
}
