package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;
import org.crosswire.jsword.versification.BibleBook;

import java.util.Optional;

public class BookIdValue implements ValueType.Definition<BibleBook> {
    @Override
    public Class<BibleBook> getValueClass() {
        return BibleBook.class;
    }

    @Override
    public BibleBook valueOf(String value) throws IllegalArgumentException {
        return Optional.ofNullable(BibleBook.fromOSIS(value))
                .orElseThrow(() -> new IllegalArgumentException("Invalid BibleBook " + value));
    }
}
