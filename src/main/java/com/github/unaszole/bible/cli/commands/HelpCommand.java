package com.github.unaszole.bible.cli.commands;

import com.github.unaszole.bible.cli.args.ScraperArgument;
import com.github.unaszole.bible.scraping.Scraper;
import picocli.CommandLine;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(name = "help", description = "Help on specific features.",
        subcommands = {  HelpCommand.ScraperList.class, HelpCommand.ScraperHelp.class })
public class HelpCommand {

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

    @CommandLine.Command(name = "scraper-list", description = "List embedded scrapers.")
    public static class ScraperList implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            findAllClassesUsingClassLoader("com.github.unaszole.bible.scraping.implementations")
                    .sorted()
                    .forEach(System.out::println);
            return 0;
        }
    }

    @CommandLine.Command(name = "scraper-help", description = "Display help for a specific scraper.")
    public static class ScraperHelp implements Callable<Integer> {

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
        ScraperArgument scraper;

        @Override
        public Integer call() throws Exception {
            Scraper.Help help = scraper.getHelp();
            if(help == null) {
                System.out.println("No help available for this scraper.");
                return 0;
            }

            System.out.println(help.description);
            if(help.inputDescriptions.isEmpty()) {
                System.out.println("No scraper inputs required.");
            }
            else {
                System.out.println("Scraper inputs :");
            }
            for (Map.Entry<String, String> input : help.inputDescriptions) {
                System.out.println("  " + input.getKey()
                        + (!input.getValue().isEmpty() ? " : " + input.getValue() : ""));
            }
            System.out.println("(Your current inputs are " + (help.inputsValid ? "valid" : "invalid") + ")");

            return 0;
        }
    }
}
