package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.datamodel.BibleRef;
import com.github.unaszole.bible.writing.interfaces.NoteTextWriter;
import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BufferedTextWrites implements Consumer<TextWriter> {

    private final List<Consumer<TextWriter>> actions = new ArrayList<>();

    public BufferedTextWrites(Consumer<TextWriter> sourceWrites) {
        sourceWrites.accept(new TextWriter() {
            @Override
            public void text(String str) {
                actions.add(w -> w.text(str));
            }

            @Override
            public void translationAdd(String str) {
                actions.add(w -> w.translationAdd(str));
            }

            @Override
            public void quote(String str) {
                actions.add(w -> w.quote(str));
            }

            @Override
            public void oldTestamentQuote(String str) {
                actions.add(w -> w.oldTestamentQuote(str));
            }

            @Override
            public void reference(BibleRef rangeStart, BibleRef rangeEnd, String text) {
                actions.add(w -> w.reference(rangeStart, rangeEnd, text));
            }

            @Override
            public void note(Consumer<NoteTextWriter> writes) {
                actions.add(w -> w.note(writes));
            }

            @Override
            public void close() {

            }
        });
    }

    @Override
    public void accept(TextWriter t) {
        for(Consumer<TextWriter> action: actions) {
            action.accept(t);
        }
    }
}
