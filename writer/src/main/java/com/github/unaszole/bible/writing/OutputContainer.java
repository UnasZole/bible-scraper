package com.github.unaszole.bible.writing;

import java.io.PrintStream;

public interface OutputContainer extends AutoCloseable {

    PrintStream createOutputStream(String outputName);

    String attach(String attachmentName, byte[] bytes);
}
