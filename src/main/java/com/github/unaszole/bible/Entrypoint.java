package com.github.unaszole.bible;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.scraping.ContextConsumer;
import com.github.unaszole.bible.scraping.Scraper;
import com.github.unaszole.bible.writing.BibleWriter;
import com.github.unaszole.bible.writing.WritingConsumer;
import com.github.unaszole.bible.writing.osis.OsisBibleWriter;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;
import org.crosswire.jsword.versification.system.SystemCatholic2;
import org.crosswire.jsword.versification.system.SystemKJV;
import org.crosswire.jsword.versification.system.Versifications;

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
                : "com.github.unaszole.bible.scraping.implementations." + className);

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

        BibleWriter writer = new OsisBibleWriter(System.out,
                Versifications.instance().getVersification(SystemCatholic2.V11N_NAME),
                "plop", "gnu", "fr");

        WritingConsumer consumer = null;

        switch (wantedMetadata.type) {
            case BIBLE:
            case BOOK:
                consumer = new WritingConsumer(writer);
                break;
            case CHAPTER:
                consumer = new WritingConsumer(writer.book(wantedMetadata.book).contents());
        }

        ContextConsumer.consumeAll(consumer, scraper.fetch(wantedMetadata));
    }

}
