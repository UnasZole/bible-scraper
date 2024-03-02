package com.github.unaszole.bible.writing;

import com.github.unaszole.bible.writing.interfaces.TextWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BufferedTextWrites implements Consumer<TextWriter> {

    private final List<Map.Entry<String, String>> actions = new ArrayList<>();

    public BufferedTextWrites(Consumer<TextWriter> sourceWrites) {
        sourceWrites.accept(new TextWriter() {
            @Override
            public void text(String str) {
                actions.add(Map.entry("text", str));
            }

            @Override
            public void note(String str) {
                actions.add(Map.entry("note", str));
            }

            @Override
            public void translationAdd(String str) {
                actions.add(Map.entry("translationAdd", str));
            }

            @Override
            public void close() {

            }
        });
    }

    @Override
    public void accept(TextWriter t) {
        for(Map.Entry<String, String> action: actions) {
            if("text".equals(action.getKey())) {
                t.text(action.getValue());
            }
            else if("note".equals(action.getKey())) {
                t.note(action.getValue());
            }
            else if("translationAdd".equals(action.getKey())) {
                t.translationAdd(action.getValue());
            }
        }
    }
}
