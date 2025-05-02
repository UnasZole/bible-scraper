package com.github.unaszole.bible.datamodel.idfieldtypes;

import com.github.unaszole.bible.datamodel.IdField;

import java.util.Optional;

public class IntegerField implements IdField.FieldType<Integer> {
    @Override
    public Class<Integer> getFieldClass() {
        return Integer.class;
    }

    @Override
    public Integer valueOf(String field) {
        return Integer.valueOf(field);
    }

    @Override
    public String toString(Integer field) {
        return field.toString();
    }

    @Override
    public Optional<Integer> getFirst() {
        return Optional.of(1);
    }

    @Override
    public Optional<Integer> getNext(Integer field) {
        return Optional.of(field + 1);
    }
}
