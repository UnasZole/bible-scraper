package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.datamodel.DocumentMetadata;
import com.github.unaszole.bible.writing.Typography;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.osis.OsisBibleWriter;
import com.github.unaszole.bible.writing.usfm.UsfmBibleWriter;
import picocli.CommandLine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class WriterArgument {

    enum WriterType { DEBUG_EVENTS, DEBUG_CTX, OSIS, USFM }

    @CommandLine.Option(names={"--writer", "-w"}, required = true, description = "Output format to write the extracted Bible. One of : OSIS, USFM")
    private WriterType writer;

    @CommandLine.Option(names = {"--outputPath", "-o"}, description = "Output file (for OSIS) or folder (for USFM). If unset, will print on stdout.")
    private Optional<Path> outputPath;

    @CommandLine.Option(names = {"--typographyFixer"}, description = "Apply typography fixes while outputting. One of : ${COMPLETION-CANDIDATES}")
    private Typography.Fixer typographyFixer = Typography.Fixer.NONE;

    public boolean isDebugEvents() {
        return writer == WriterType.DEBUG_EVENTS;
    }
    public boolean isDebugCtx() {
        return writer == WriterType.DEBUG_CTX;
    }

    public UnaryOperator<String> getTypographyFixer() {
        return Typography.getFixer(typographyFixer);
    }

    public BibleWriter get(DocumentMetadata docMeta) throws Exception {
        switch(writer) {
            case OSIS:
                OutputStream os = outputPath.isPresent() ? Files.newOutputStream(outputPath.get()) : System.out;
                return new OsisBibleWriter(os, docMeta);

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
