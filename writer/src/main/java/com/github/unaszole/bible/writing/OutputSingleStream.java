package com.github.unaszole.bible.writing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputSingleStream implements OutputContainer {

    private static class NoCloseWrappingOutputStream extends OutputStream {
        private final OutputStream wrapped;

        private NoCloseWrappingOutputStream(OutputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void write(int b) throws IOException {
            wrapped.write(b);
        }
    }

    private final OutputStream outStream;

    public OutputSingleStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public PrintStream createOutputStream(String outputName) {
        // Return the existing output stream, ensuring it won't be closed.
        return new PrintStream(new NoCloseWrappingOutputStream(outStream));
    }

    @Override
    public String attach(String attachmentName, byte[] bytes) {
        // Attachments are not rendered in a single stream output.
        return attachmentName;
    }

    @Override
    public void close() throws Exception {
        // Output stream was received in input, do not close it.
    }
}
