package com.github.unaszole.bible.cli;

import com.github.unaszole.bible.writing.BibleWriter;
import com.github.unaszole.bible.writing.osis.OsisBibleWriter;
import com.github.unaszole.bible.writing.usfm.UsfmBibleWriter;
import org.crosswire.jsword.versification.system.SystemCatholic2;
import org.crosswire.jsword.versification.system.Versifications;
import picocli.CommandLine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class WriterArgument {

    enum WriterType { DEBUG, OSIS, USFM }

    @CommandLine.Option(names={"--writer", "-w"}, required = true)
    private WriterType writer;

    @CommandLine.Option(names = {"--outputPath", "-o"})
    private Optional<Path> outputPath;

    public boolean isDebug() {
        return writer == WriterType.DEBUG;
    }

    public BibleWriter get() throws Exception {
        switch(writer) {
            case OSIS:
                OutputStream os = outputPath.isPresent() ? Files.newOutputStream(outputPath.get()) : System.out;
                return new OsisBibleWriter(os,
                        Versifications.instance().getVersification(SystemCatholic2.V11N_NAME),
                        "plop", "gnu", "fr");

            case USFM:
                if(outputPath.isPresent()) {
                    return new UsfmBibleWriter(outputPath.get());
                }
                return new UsfmBibleWriter(new PrintWriter(System.out));

            default:
                return null;
        }
    }
}
