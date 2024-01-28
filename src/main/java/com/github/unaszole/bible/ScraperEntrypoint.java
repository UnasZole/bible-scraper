package com.github.unaszole.bible;

import com.github.unaszole.bible.cli.ScraperArgument;
import com.github.unaszole.bible.cli.WantedMetadataArgument;
import com.github.unaszole.bible.cli.WriterArgument;
import com.github.unaszole.bible.writing.BibleWriter;
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class ScraperEntrypoint implements Callable<Integer> {

    @CommandLine.Option(names="--cachePath")
    Optional<Path> cachePath;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
    WantedMetadataArgument wantedMetadata;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
    ScraperArgument scraper;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
    WriterArgument writer;

    @Override
    public Integer call() throws Exception {
        Path cachePath = this.cachePath.orElseGet(
                () -> FileSystems.getDefault()
                        .getPath(System.getProperty("java.io.tmpdir"))
                        .resolve("bible-scraper")
        );
        Files.createDirectories(cachePath);

        if(writer.isDebug()) {
            System.out.println(scraper.get(cachePath).stream(wantedMetadata.get()).extractContext());
        }
        else {
            ContextStreamWriter streamWriter = new ContextStreamWriter(
                    scraper.get(cachePath).stream(wantedMetadata.get()).getStream()
            );
            try(BibleWriter bibleWriter = writer.get()) {
                streamWriter.writeBibleSubset(bibleWriter, wantedMetadata.get());
            }
        }

        System.out.println();

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new ScraperEntrypoint()).execute(args);
        System.exit(exitCode);
    }
}
