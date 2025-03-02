package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

public class StringValue implements ValueType<String> {
    @Override
    public Class<String> getValueClass() {
        return String.class;
    }

    @Override
    public String valueOf(String value) throws IllegalArgumentException {
        return value;
    }
}
