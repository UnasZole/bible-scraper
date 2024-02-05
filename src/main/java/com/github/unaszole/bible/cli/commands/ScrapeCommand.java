package com.github.unaszole.bible.cli.commands;

import com.github.unaszole.bible.ScraperEntrypoint;
import com.github.unaszole.bible.cli.args.ScraperArgument;
import com.github.unaszole.bible.cli.args.WantedMetadataArgument;
import com.github.unaszole.bible.cli.args.WriterArgument;
import com.github.unaszole.bible.writing.ContextStreamWriter;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "scrape")
public class ScrapeCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    boolean usageHelpRequested;

    @CommandLine.ParentCommand
    ScraperEntrypoint entrypoint;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1", heading = "\n== Which portion of the Bible to extract?\n")
    WantedMetadataArgument wantedMetadata;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1", heading = "\n== Where to fetch the Bible from?\n")
    ScraperArgument scraper;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1", heading = "\n== How to export the Bible?\n")
    WriterArgument writer;

    @Override
    public Integer call() throws Exception {
        Path cachePath = entrypoint.getCachePath();
        Files.createDirectories(cachePath);

        if(writer.isDebugEvents()) {
            scraper.get(cachePath).stream(wantedMetadata.get()).getStream().forEachOrdered(
                    System.out::println
            );
        }
        else if(writer.isDebugCtx()) {
            System.out.println(scraper.get(cachePath).stream(wantedMetadata.get()).extractContext());
        }
        else {
            ContextStreamWriter streamWriter = new ContextStreamWriter(
                    scraper.get(cachePath).stream(wantedMetadata.get()).getStream(),
                    writer.getTypographyFixer()
            );
            try(BibleWriter bibleWriter = writer.get()) {
                streamWriter.writeBibleSubset(bibleWriter, wantedMetadata.get());
            }
        }

        System.out.println();

        return 0;
    }
}
