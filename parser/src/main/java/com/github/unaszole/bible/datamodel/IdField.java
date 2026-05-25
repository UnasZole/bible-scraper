package com.github.unaszole.bible.datamodel;

import java.util.Map;

public interface IdField {
    String name();
    ValueType<?> valueType();

    default Map.Entry<IdField, ?> of(Object value) {
        return Map.entry(this, valueType().valueOf(value));
    }
}
