package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IntegerListValue extends ValueType.ClassBasedSequential<List<Integer>> {
    private static final String SEPARATOR = ",";

    public IntegerListValue() {
        super((Class<List<Integer>>)(Class<?>) List.class,
                s -> Arrays.stream(s.split("[^a-zA-Z0-9]+"))
                        .map(IntegerValue::parseInt)
                        .collect(Collectors.toList()),
                List.of(1),
                previous -> previous.stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .stream()
                        .mapToObj(i -> i + 1)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public String toString(List<Integer> value) {
        return value.stream()
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(SEPARATOR));
    }
}
