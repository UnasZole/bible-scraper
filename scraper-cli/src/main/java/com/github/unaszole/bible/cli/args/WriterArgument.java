package com.github.unaszole.bible.cli.args;

import com.github.unaszole.bible.monitor.ExecutionMonitor;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.writing.Typography;
import com.github.unaszole.bible.writing.interfaces.BibleWriter;
import com.github.unaszole.bible.writing.mybible.MyBibleBibleWriter;
import com.github.unaszole.bible.writing.osis.OsisBibleWriter;
import com.github.unaszole.bible.writing.usfm.UsfmBibleWriter;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class WriterArgument {

    enum WriterType { DEBUG_EVENTS, DEBUG_CTX, OSIS, USFM, MYBIBLE }

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

    private void printStatus(ExecutionMonitor.Status status) {
        System.out.printf("### Scraping: %5s / %s - %10s \r", status.completedItems, status.registeredItems, status.lastStartedItem);
    }

    public BibleWriter get(DocumentMetadata docMeta) throws Exception {
        switch(writer) {
            case OSIS:
                if(outputPath.isPresent()) {
                    // We're not using stdout for actual output : plug the progress bar.
                    ExecutionMonitor.INSTANCE.registerUpdateCallback(this::printStatus);
                    return new OsisBibleWriter(outputPath.get(), docMeta);
                }
                return new OsisBibleWriter(System.out, docMeta);

            case USFM:
                if(outputPath.isPresent()) {
                    // We're not using stdout for actual output : plug the progress bar.
                    ExecutionMonitor.INSTANCE.registerUpdateCallback(this::printStatus);
                    return new UsfmBibleWriter(outputPath.get());
                }
                return new UsfmBibleWriter(new PrintWriter(System.out));

            case MYBIBLE:
                if(outputPath.isPresent()) {
                    return new MyBibleBibleWriter(outputPath.get(), docMeta);
                }
                return new MyBibleBibleWriter(new PrintWriter(System.out));

            default:
                return null;
        }
    }
}
