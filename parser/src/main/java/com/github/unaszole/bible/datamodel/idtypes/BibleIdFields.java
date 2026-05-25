package com.github.unaszole.bible.datamodel.idtypes;

import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.datamodel.ValueType;
import com.github.unaszole.bible.datamodel.valuetypes.StdValueTypes;

public enum BibleIdFields implements IdField {
    BOOK(StdValueTypes.BIBLE_BOOK),
    CHAPTER(StdValueTypes.INTEGER),
    VERSES(StdValueTypes.INTEGER_LIST);

    private final ValueType<?> valueType;

    BibleIdFields(ValueType<?> valueType) {
        this.valueType = valueType;
    }

    @Override
    public ValueType<?> valueType() {
        return valueType;
    }
}
