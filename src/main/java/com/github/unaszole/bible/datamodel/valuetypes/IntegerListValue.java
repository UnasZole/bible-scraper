package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

import java.util.Arrays;

public class IntegerListValue implements ValueType<int[]> {
    @Override
    public Class<int[]> getValueClass() {
        return int[].class;
    }

    @Override
    public int[] valueOf(String value) throws IllegalArgumentException {
        return Arrays.stream(value.split("[^a-zA-Z0-9]+"))
                .mapToInt(IntegerValue::parseInt)
                .toArray();
    }
}
