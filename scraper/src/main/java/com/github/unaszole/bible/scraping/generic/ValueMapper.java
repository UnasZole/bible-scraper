package com.github.unaszole.bible.scraping.generic;

import com.github.unaszole.bible.datamodel.ValueType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValueMapper {

    private final Map<ValueType<?>, ValueTypeMapper<?>> stringToValueMappers = new HashMap<>();

    public <ValueClass> ValueMapper withMapper(ValueType<ValueClass> valueType, ValueTypeMapper<ValueClass> mapper) {
        ValueMapper newMapper = new ValueMapper();
        newMapper.stringToValueMappers.putAll(stringToValueMappers);
        newMapper.stringToValueMappers.put(valueType, mapper);
        return newMapper;
    }

    public <ValueClass> ValueClass mapToValue(ValueType<ValueClass> valueType, String valueString) {
        // Attempt value resolution from a registered mapper and,
        // if it fails, attempt standard value resolution for the value type.
        return Optional.ofNullable(stringToValueMappers.get(valueType))
                .flatMap(mapper -> mapper.map(valueString))
                .map(v -> (ValueClass) v)
                .orElseGet(() -> valueType.valueOf(valueString));
    }
}
