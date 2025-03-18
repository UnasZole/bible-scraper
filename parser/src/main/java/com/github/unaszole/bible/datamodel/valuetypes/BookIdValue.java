package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;
import org.crosswire.jsword.versification.BibleBook;

public class BookIdValue implements ValueType.Definition<BibleBook> {
    @Override
    public Class<BibleBook> getValueClass() {
        return BibleBook.class;
    }

    @Override
    public BibleBook valueOf(String value) throws IllegalArgumentException {
        return BibleBook.fromOSIS(value);
    }
}
