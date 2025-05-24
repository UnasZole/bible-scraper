package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MyBibleBookWriter implements BookWriter {

    public static final Map<BibleBook, Integer> OSIS_TO_MYBIBLE = new HashMap<>();
    static {
        OSIS_TO_MYBIBLE.put(BibleBook.GEN, 1);
        OSIS_TO_MYBIBLE.put(BibleBook.EXOD, 2);
        OSIS_TO_MYBIBLE.put(BibleBook.LEV, 3);
        OSIS_TO_MYBIBLE.put(BibleBook.NUM, 4);
        OSIS_TO_MYBIBLE.put(BibleBook.DEUT, 5);

        OSIS_TO_MYBIBLE.put(BibleBook.JOSH, 6);
        OSIS_TO_MYBIBLE.put(BibleBook.JUDG, 7);
        OSIS_TO_MYBIBLE.put(BibleBook.RUTH, 8);
        OSIS_TO_MYBIBLE.put(BibleBook.SAM1, 9);
        OSIS_TO_MYBIBLE.put(BibleBook.SAM2, 10);

        OSIS_TO_MYBIBLE.put(BibleBook.KGS1, 11);
        OSIS_TO_MYBIBLE.put(BibleBook.KGS2, 12);
        OSIS_TO_MYBIBLE.put(BibleBook.CHR1, 13);
        OSIS_TO_MYBIBLE.put(BibleBook.CHR2, 14);
        OSIS_TO_MYBIBLE.put(BibleBook.EZRA, 15);

        OSIS_TO_MYBIBLE.put(BibleBook.NEH, 16);
        OSIS_TO_MYBIBLE.put(BibleBook.ESTH, 17);
        OSIS_TO_MYBIBLE.put(BibleBook.JOB, 18);
        OSIS_TO_MYBIBLE.put(BibleBook.PS, 19);
        OSIS_TO_MYBIBLE.put(BibleBook.PROV, 20);

        OSIS_TO_MYBIBLE.put(BibleBook.ECCL, 21);
        OSIS_TO_MYBIBLE.put(BibleBook.SONG, 22);
        OSIS_TO_MYBIBLE.put(BibleBook.ISA, 23);
        OSIS_TO_MYBIBLE.put(BibleBook.JER, 24);
        OSIS_TO_MYBIBLE.put(BibleBook.LAM, 25);

        OSIS_TO_MYBIBLE.put(BibleBook.EZEK, 26);
        OSIS_TO_MYBIBLE.put(BibleBook.DAN, 27);
        OSIS_TO_MYBIBLE.put(BibleBook.HOS, 28);
        OSIS_TO_MYBIBLE.put(BibleBook.JOEL, 29);
        OSIS_TO_MYBIBLE.put(BibleBook.AMOS, 30);

        OSIS_TO_MYBIBLE.put(BibleBook.OBAD, 31);
        OSIS_TO_MYBIBLE.put(BibleBook.JONAH, 32);
        OSIS_TO_MYBIBLE.put(BibleBook.MIC, 33);
        OSIS_TO_MYBIBLE.put(BibleBook.NAH, 34);
        OSIS_TO_MYBIBLE.put(BibleBook.HAB, 35);

        OSIS_TO_MYBIBLE.put(BibleBook.ZEPH, 36);
        OSIS_TO_MYBIBLE.put(BibleBook.HAG, 37);
        OSIS_TO_MYBIBLE.put(BibleBook.ZECH, 38);
        OSIS_TO_MYBIBLE.put(BibleBook.MAL, 39);
        OSIS_TO_MYBIBLE.put(BibleBook.MATT, 40);

        OSIS_TO_MYBIBLE.put(BibleBook.MARK, 41);
        OSIS_TO_MYBIBLE.put(BibleBook.LUKE, 42);
        OSIS_TO_MYBIBLE.put(BibleBook.JOHN, 43);
        OSIS_TO_MYBIBLE.put(BibleBook.ACTS, 44);
        OSIS_TO_MYBIBLE.put(BibleBook.ROM, 45);

        OSIS_TO_MYBIBLE.put(BibleBook.COR1, 46);
        OSIS_TO_MYBIBLE.put(BibleBook.COR2, 47);
        OSIS_TO_MYBIBLE.put(BibleBook.GAL, 48);
        OSIS_TO_MYBIBLE.put(BibleBook.EPH, 49);
        OSIS_TO_MYBIBLE.put(BibleBook.PHIL, 50);

        OSIS_TO_MYBIBLE.put(BibleBook.COL, 51);
        OSIS_TO_MYBIBLE.put(BibleBook.THESS1, 52);
        OSIS_TO_MYBIBLE.put(BibleBook.THESS2, 53);
        OSIS_TO_MYBIBLE.put(BibleBook.TIM1, 54);
        OSIS_TO_MYBIBLE.put(BibleBook.TIM2, 55);

        OSIS_TO_MYBIBLE.put(BibleBook.TITUS, 56);
        OSIS_TO_MYBIBLE.put(BibleBook.PHLM, 57);
        OSIS_TO_MYBIBLE.put(BibleBook.HEB, 58);
        OSIS_TO_MYBIBLE.put(BibleBook.JAS, 59);
        OSIS_TO_MYBIBLE.put(BibleBook.PET1, 60);

        OSIS_TO_MYBIBLE.put(BibleBook.PET2, 61);
        OSIS_TO_MYBIBLE.put(BibleBook.JOHN1, 62);
        OSIS_TO_MYBIBLE.put(BibleBook.JOHN2, 63);
        OSIS_TO_MYBIBLE.put(BibleBook.JOHN3, 64);
        OSIS_TO_MYBIBLE.put(BibleBook.JUDE, 65);

        OSIS_TO_MYBIBLE.put(BibleBook.REV, 66);
        OSIS_TO_MYBIBLE.put(BibleBook.TOB, 67);
        OSIS_TO_MYBIBLE.put(BibleBook.JDT, 68);
        OSIS_TO_MYBIBLE.put(BibleBook.WIS, 69);
        OSIS_TO_MYBIBLE.put(BibleBook.SIR, 70);

        OSIS_TO_MYBIBLE.put(BibleBook.BAR, 71);
        OSIS_TO_MYBIBLE.put(BibleBook.MACC1, 72);
        OSIS_TO_MYBIBLE.put(BibleBook.MACC2, 73);
    }

    private final MyBibleBookContentsWriter writer;

    public MyBibleBookWriter(VerseSink sink, BibleBook book) {
        this.writer = new MyBibleBookContentsWriter(sink, OSIS_TO_MYBIBLE.get(book));
    }

    @Override
    public void title(String title) {
        // MyBible does not support book metadata.
    }

    @Override
    public void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes) {
        // MyBible does not support book metadata.
    }

    @Override
    public void contents(Consumer<StructuredTextWriter.BookContentsWriter> writes) {
        writes.accept(writer);
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
