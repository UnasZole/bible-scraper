package com.github.unaszole.bible.osisbuilder;

import com.github.unaszole.bible.osisbuilder.parser.ContextMetadata;
import com.github.unaszole.bible.osisbuilder.parser.Scraper;
import org.crosswire.jsword.versification.BibleBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Entrypoint {

    private static Scraper getScraper(Path cachePath, String scraperName) throws Exception {
        String[] scraperNameSplit = scraperName.split("#");
        String className = scraperNameSplit[0];
        String[] flags = Arrays.copyOfRange(scraperNameSplit, 1, scraperNameSplit.length);

        Class<? extends Scraper> scraperClass = (Class<? extends Scraper>) Class.forName(className.contains(".")
                ? className
                : "com.github.unaszole.bible.implementations.scrapers." + className);

        return scraperClass.getConstructor(Path.class, String[].class).newInstance(cachePath, flags);
    }

    public static void main(String[] args) throws Exception {
        Path cachePath = Files.createDirectories(Paths.get(args[0]));
        Scraper scraper = getScraper(cachePath, args[1]);

        ContextMetadata wantedMetadata;
        if(args.length >= 4) {
            wantedMetadata = ContextMetadata.forChapter(BibleBook.fromOSIS(args[2]), Integer.valueOf(args[3]));
        }
        else if(args.length >= 3) {
            wantedMetadata = ContextMetadata.forBook(BibleBook.fromOSIS(args[2]));
        }
        else {
            wantedMetadata = ContextMetadata.forBible();
        }

        System.out.println(scraper.fetch(wantedMetadata));
    }

}
