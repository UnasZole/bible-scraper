package com.github.unaszole.bible.writing;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class OutputDirectory implements OutputContainer {

    public final Path path;

    public OutputDirectory(Path path) throws IOException {
        this.path = path;

        if(!Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " must be an (empty) directory.");
        }

        try(Stream<Path> children = Files.list(path)) {
            if(children.findAny().isPresent()) {
                throw new IllegalArgumentException(path + " directory must be empty.");
            }
        }
    }

    private String getUnusedName(String requestedName) {
        String fileName = requestedName;
        while(Files.exists(path.resolve(fileName))) {
            fileName = "_" + fileName;
        }
        return fileName;
    }

    public PrintStream createOutputStream(String outputName) {
        String fileName = getUnusedName(outputName);
        try {
            return new PrintStream(Files.newOutputStream(
                    path.resolve(fileName),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String attach(String attachmentName, byte[] bytes) {
        String fileName = getUnusedName(attachmentName);
        try {
            Files.write(path.resolve(fileName), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileName;
    }

    @Override
    public void close() throws Exception {
        // Each output stream was created on demand and should already be closed by its owner. Nothing to do.
    }
}
