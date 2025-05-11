package com.github.unaszole.bible;

import com.github.unaszole.bible.cli.commands.HelpCommand;
import com.github.unaszole.bible.cli.commands.ScrapeCommand;
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

@CommandLine.Command(name = "bible-scraper", subcommands = { ScrapeCommand.class, HelpCommand.class })
public class ScraperEntrypoint {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    boolean usageHelpRequested;

    @CommandLine.Option(names="--cachePath", description = "Folder in which downloaded pages should be cached. If omitted, it will create a folder in your temporary directory.")
    public Optional<Path> cachePath;

    public Path getCachePath() {
        return cachePath.orElseGet(
                () -> FileSystems.getDefault()
                        .getPath(System.getProperty("java.io.tmpdir"))
                        .resolve("bible-scraper")
        );
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new ScraperEntrypoint()).execute(args);
        System.exit(exitCode);
    }
}
