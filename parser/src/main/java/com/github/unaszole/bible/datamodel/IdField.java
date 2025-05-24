package com.github.unaszole.bible.datamodel;

import java.util.Map;

public class IdField<ValueClass> {
    public final ValueType<ValueClass> type;

    public IdField(ValueType<ValueClass> type) {
        this.type = type;
    }

    public Map.Entry<IdField<?>, ?> of(Object value) {
        return Map.entry(this, type.valueOf(value));
    }
}
