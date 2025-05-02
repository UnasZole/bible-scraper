package com.github.unaszole.bible.datamodel.idfieldtypes;

import com.github.unaszole.bible.datamodel.IdField;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IntegerListField implements IdField.FieldType<List<Integer>> {
    private static final String SEPARATOR = ",";

    @Override
    public Class<List<Integer>> getFieldClass() {
        return (Class<List<Integer>>)(Class<?>) List.class;
    }

    @Override
    public List<Integer> valueOf(String field) {
        return Arrays.stream(field.split(SEPARATOR))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public String toString(List<Integer> field) {
        return field.stream()
                .map(Object::toString)
                .collect(Collectors.joining(SEPARATOR));
    }

    @Override
    public Optional<List<Integer>> getFirst() {
        return Optional.of(List.of(1));
    }

    @Override
    public Optional<List<Integer>> getNext(List<Integer> field) {
        return field.stream()
                .mapToInt(Integer::intValue)
                .max()
                .stream()
                .boxed()
                .findAny()
                .map(max -> List.of(max + 1));
    }
}
