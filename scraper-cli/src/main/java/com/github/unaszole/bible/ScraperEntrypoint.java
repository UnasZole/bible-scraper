package com.github.unaszole.bible;

import com.github.unaszole.bible.cli.args.ScraperArgument;
import com.github.unaszole.bible.cli.args.WantedMetadataArgument;
import com.github.unaszole.bible.cli.args.WriterArgument;
import com.github.unaszole.bible.cli.commands.HelpCommand;
import com.github.unaszole.bible.cli.commands.ScrapeCommand;
import com.github.unaszole.bible.writing.ContextStreamWriter;
import com.github.unaszole.bible.writing.Typography;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

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
