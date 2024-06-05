package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.writing.interfaces.BookWriter;
import com.github.unaszole.bible.writing.interfaces.StructuredTextWriter;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UsfmBookWriter implements BookWriter {

    private static final Map<BibleBook, String> OSIS_TO_USFM = new HashMap<>();
    static {
        OSIS_TO_USFM.put(BibleBook.GEN, "GEN");
        OSIS_TO_USFM.put(BibleBook.EXOD, "EXO");
        OSIS_TO_USFM.put(BibleBook.LEV, "LEV");
        OSIS_TO_USFM.put(BibleBook.NUM, "NUM");
        OSIS_TO_USFM.put(BibleBook.DEUT, "DEU");
        OSIS_TO_USFM.put(BibleBook.JOSH, "JOS");
        OSIS_TO_USFM.put(BibleBook.JUDG, "JDG");
        OSIS_TO_USFM.put(BibleBook.RUTH, "RUT");
        OSIS_TO_USFM.put(BibleBook.SAM1, "1SA");
        OSIS_TO_USFM.put(BibleBook.SAM2, "2SA");
        OSIS_TO_USFM.put(BibleBook.KGS1, "1KI");
        OSIS_TO_USFM.put(BibleBook.KGS2, "2KI");
        OSIS_TO_USFM.put(BibleBook.CHR1, "1CH");
        OSIS_TO_USFM.put(BibleBook.CHR2, "2CH");
        OSIS_TO_USFM.put(BibleBook.EZRA, "EZR");
        OSIS_TO_USFM.put(BibleBook.NEH, "NEH");
        OSIS_TO_USFM.put(BibleBook.ESTH, "EST");
        OSIS_TO_USFM.put(BibleBook.JOB, "JOB");
        OSIS_TO_USFM.put(BibleBook.PS, "PSA");
        OSIS_TO_USFM.put(BibleBook.PROV, "PRO");
        OSIS_TO_USFM.put(BibleBook.ECCL, "ECC");
        OSIS_TO_USFM.put(BibleBook.SONG, "SNG");
        OSIS_TO_USFM.put(BibleBook.ISA, "ISA");
        OSIS_TO_USFM.put(BibleBook.JER, "JER");
        OSIS_TO_USFM.put(BibleBook.LAM, "LAM");
        OSIS_TO_USFM.put(BibleBook.EZEK, "EZK");
        OSIS_TO_USFM.put(BibleBook.DAN, "DAN");
        OSIS_TO_USFM.put(BibleBook.HOS, "HOS");
        OSIS_TO_USFM.put(BibleBook.JOEL, "JOL");
        OSIS_TO_USFM.put(BibleBook.AMOS, "AMO");
        OSIS_TO_USFM.put(BibleBook.OBAD, "OBA");
        OSIS_TO_USFM.put(BibleBook.JONAH, "JON");
        OSIS_TO_USFM.put(BibleBook.MIC, "MIC");
        OSIS_TO_USFM.put(BibleBook.NAH, "NAM");
        OSIS_TO_USFM.put(BibleBook.HAB, "HAB");
        OSIS_TO_USFM.put(BibleBook.ZEPH, "ZEP");
        OSIS_TO_USFM.put(BibleBook.HAG, "HAG");
        OSIS_TO_USFM.put(BibleBook.ZECH, "ZEC");
        OSIS_TO_USFM.put(BibleBook.MAL, "MAL");
        OSIS_TO_USFM.put(BibleBook.MATT, "MAT");
        OSIS_TO_USFM.put(BibleBook.MARK, "MRK");
        OSIS_TO_USFM.put(BibleBook.LUKE, "LUK");
        OSIS_TO_USFM.put(BibleBook.JOHN, "JHN");
        OSIS_TO_USFM.put(BibleBook.ACTS, "ACT");
        OSIS_TO_USFM.put(BibleBook.ROM, "ROM");
        OSIS_TO_USFM.put(BibleBook.COR1, "1CO");
        OSIS_TO_USFM.put(BibleBook.COR2, "2CO");
        OSIS_TO_USFM.put(BibleBook.GAL, "GAL");
        OSIS_TO_USFM.put(BibleBook.EPH, "EPH");
        OSIS_TO_USFM.put(BibleBook.PHIL, "PHP");
        OSIS_TO_USFM.put(BibleBook.COL, "COL");
        OSIS_TO_USFM.put(BibleBook.THESS1, "1TH");
        OSIS_TO_USFM.put(BibleBook.THESS2, "2TH");
        OSIS_TO_USFM.put(BibleBook.TIM1, "1TI");
        OSIS_TO_USFM.put(BibleBook.TIM2, "2TI");
        OSIS_TO_USFM.put(BibleBook.TITUS, "TIT");
        OSIS_TO_USFM.put(BibleBook.PHLM, "PHM");
        OSIS_TO_USFM.put(BibleBook.HEB, "HEB");
        OSIS_TO_USFM.put(BibleBook.JAS, "JAS");
        OSIS_TO_USFM.put(BibleBook.PET1, "1PE");
        OSIS_TO_USFM.put(BibleBook.PET2, "2PE");
        OSIS_TO_USFM.put(BibleBook.JOHN1, "1JN");
        OSIS_TO_USFM.put(BibleBook.JOHN2, "2JN");
        OSIS_TO_USFM.put(BibleBook.JOHN3, "3JN");
        OSIS_TO_USFM.put(BibleBook.JUDE, "JUD");
        OSIS_TO_USFM.put(BibleBook.REV, "REV");
        OSIS_TO_USFM.put(BibleBook.TOB, "TOB");
        OSIS_TO_USFM.put(BibleBook.JDT, "JDT");
        OSIS_TO_USFM.put(BibleBook.ESTH_GR, "ESG");
        OSIS_TO_USFM.put(BibleBook.ADD_ESTH, "ESG");
        OSIS_TO_USFM.put(BibleBook.WIS, "WIS");
        OSIS_TO_USFM.put(BibleBook.SIR, "SIR");
        OSIS_TO_USFM.put(BibleBook.BAR, "BAR");
        OSIS_TO_USFM.put(BibleBook.EP_JER, "LJE");
        OSIS_TO_USFM.put(BibleBook.PR_AZAR, "S3Y");
        OSIS_TO_USFM.put(BibleBook.SUS, "SUS");
        OSIS_TO_USFM.put(BibleBook.BEL, "BEL");
        OSIS_TO_USFM.put(BibleBook.MACC1, "1MA");
        OSIS_TO_USFM.put(BibleBook.MACC2, "2MA");
        OSIS_TO_USFM.put(BibleBook.ADD_DAN, "DAG");
        OSIS_TO_USFM.put(BibleBook.PR_MAN, "MAN");
        OSIS_TO_USFM.put(BibleBook.ESD1, "1ES");
        OSIS_TO_USFM.put(BibleBook.ESD2, "2ES");
    }

    private final PrintWriter out;
    private final boolean closeOut;

    private UsfmBookWriter(BibleBook book, PrintWriter out, boolean closeOut) {
        this.out = out;
        this.closeOut = closeOut;

        out.println("\\id " + OSIS_TO_USFM.get(book));
    }

    public UsfmBookWriter(BibleBook book, PrintWriter out) {
        // Do not close the outWriter : it was provided externally, so it's the caller's job to manage it.
        this(book, out, false);
    }

    public UsfmBookWriter(BibleBook book, int bookNb, Path outFolder) throws IOException {
        this(book, new PrintWriter(Files.newBufferedWriter(
                outFolder.resolve(String.format("%02d", bookNb) + "_" + book.getOSIS() + ".usfm")
        )), true);
    }

    @Override
    public void title(String title) {
        out.println("\\mt1 " + title);
    }

    @Override
    public void introduction(Consumer<StructuredTextWriter.BookIntroWriter> writes) {
        try(StructuredTextWriter.BookIntroWriter introWriter = new UsfmBookIntroWriter(out)) {
            writes.accept(introWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contents(Consumer<StructuredTextWriter.BookContentsWriter> writes) {
        try(StructuredTextWriter.BookContentsWriter contentsWriter = new UsfmBookContentsWriter(out)) {
            writes.accept(contentsWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        out.flush();
        if(closeOut) {
            out.close();
        }
    }
}
