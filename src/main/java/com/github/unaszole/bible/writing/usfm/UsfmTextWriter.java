package com.github.unaszole.bible.writing.usfm;

import com.github.unaszole.bible.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;

public class UsfmTextWriter implements TextWriter {

    protected final PrintWriter out;
    public UsfmTextWriter(PrintWriter out) {
        this.out = out;
    }

    private String activeTagName = null;

    protected void printTag(String tagName, String contents, boolean close) {
        String tag = "\\" + tagName;
        if(Objects.equals(tagName, activeTagName)) {
            // We're already in the requested tag, just print the contents.
            out.print(contents);
        }
        else {
            // Else, we need an opening tag before the contents.

            // If we are already in an active tag, and we're going to close this one after the call, it's a nested tag.
            if(activeTagName != null && close) {
                tag = "\\+" + tagName;
            }

            // If contents start with a space, move it before the tag as prefix.
            String prefix = "";
            String str = contents;
            if(str.startsWith(" ")) {
                prefix = " ";
                str = str.substring(1);
            }

            out.print(prefix + tag + " " + str);
        }

        if(close) {
            out.print(tag + "*");
            if(Objects.equals(tagName, activeTagName)) {
                // If we closed a previously active tag, there is no active tag anymore.
                activeTagName = null;
            }
        }
        else {
            // We leave the tag opened.
            activeTagName = tagName;
        }
    }

    @Override
    public void text(String str) {
        out.print(str);
    }

    @Override
    public void translationAdd(String str) {
        printTag("add", str, true);
    }

    @Override
    public void quote(String str) {
        printTag("qt", str, true);
    }

    protected static String getUsfmRef(BibleRef ref) {
        return UsfmBookWriter.OSIS_TO_USFM.get(ref.book)
                + (ref.chapter == 0 ? "" : " " + ref.chapter
                + (ref.verse == 0 ? "" : ":" + ref.verse));
    }

    @Override
    public void reference(BibleRef rangeStart, BibleRef rangeEnd, String text) {
        printTag("xt", text + "|" + getUsfmRef(rangeStart)
                + (rangeEnd != null && rangeEnd.verse > 0 ? "-" + rangeEnd.verse : "")
                , true);
    }

    @Override
    public void note(Consumer<NoteTextWriter> writes) {
        out.print("\\f + ");
        writes.accept(new UsfmNoteTextWriter(out));
        out.print("\\f*");
    }

    @Override
    public void close() {
        out.flush();
    }
}
