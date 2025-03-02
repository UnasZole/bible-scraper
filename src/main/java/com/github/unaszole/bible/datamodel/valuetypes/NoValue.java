package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

public class NoValue implements ValueType<Void> {
    @Override
    public Class<Void> getValueClass() {
        return Void.class;
    }

    @Override
    public Void valueOf(String value) {
        if(value != null) {
            throw new IllegalArgumentException("Received value " + value + " for a context with no value");
        }
        return null;
    }

    @Override
    public Void of(Object value) throws IllegalArgumentException {
        if(value != null) {
            throw new IllegalArgumentException("Received value " + value + " for a context with no value");
        }
        return null;
    }
}
