package com.github.unaszole.bible.datamodel.idfieldtypes;

import com.github.unaszole.bible.datamodel.IdField;
import org.crosswire.jsword.versification.BibleBook;

public class BibleBookField implements IdField.FieldType<BibleBook> {
    @Override
    public Class<BibleBook> getFieldClass() {
        return BibleBook.class;
    }

    @Override
    public BibleBook valueOf(String field) {
        return BibleBook.fromOSIS(field);
    }

    @Override
    public String toString(BibleBook field) {
        return field.getOSIS();
    }
}
