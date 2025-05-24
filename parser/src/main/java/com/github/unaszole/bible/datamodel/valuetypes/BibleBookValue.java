package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;
import org.crosswire.jsword.versification.BibleBook;

import java.util.Optional;

public class BibleBookValue extends ValueType.ClassBased<BibleBook> {

    public BibleBookValue() {
        super(BibleBook.class, s -> Optional.ofNullable(BibleBook.fromOSIS(s))
                .orElseThrow(() -> new IllegalArgumentException("Invalid BibleBook " + s)));
    }

    @Override
    public String toString(BibleBook value) {
        return value.getOSIS();
    }
}
