package com.github.unaszole.bible;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class JarUtils {
    public static Stream<Path> listResources(String dirPath) throws Exception {
        URI pkg = ClassLoader.getSystemClassLoader().getResource(dirPath).toURI();
        try (FileSystem fs = FileSystems.newFileSystem(pkg, Collections.emptyMap())) {
            return Files.list(fs.getPath(dirPath));
        }
    }

    public static Stream<String> findAllClassesUsingClassLoader(String packageName) throws Exception {
        String pkgPath = packageName.replaceAll("[.]", "/");
        return listResources(pkgPath).map(Path::getFileName)
                .map(Path::toString)
                .filter(s -> s.endsWith(".class") && !s.contains("$"))
                .map(s -> s.substring(0, s.lastIndexOf('.')));
    }

    public static Stream<String> listScrapers() throws Exception {
        return findAllClassesUsingClassLoader("com.github.unaszole.bible.scraping.implementations");
    }
}
