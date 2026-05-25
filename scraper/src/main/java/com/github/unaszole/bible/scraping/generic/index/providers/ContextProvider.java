package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.*;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record ContextProvider(
        @Nonnull String contextTypeName,
        @Nonnull Map<String, String> idFieldPatterns,
        @Nullable String valuePattern
) {
    private ContextId buildContextId(ContextType contextType, PatternConfig patternConfig, Map<IdField, Object> inheritedIdFields, ValueMapper mapper) {
        assert contextType.idType() != IdType.NO_ID;
        Map<IdField, Object> idFieldValues = new HashMap<>();
        for (IdField idField : contextType.idType().fields) {
            String fieldPattern = idFieldPatterns.get(idField.name());
            if (fieldPattern != null) {
                // Pattern is set for this field, use it.
                idFieldValues.put(idField, mapper.mapToValue(
                        idField.valueType(),
                        patternConfig.evaluate(fieldPattern)
                                .orElseThrow(() -> new IllegalArgumentException("Pattern " + fieldPattern + " not available to resolve ID field " + idField))
                ));
            } else {
                // Inherit the field from the ancestor.
                idFieldValues.put(idField, Optional.ofNullable(inheritedIdFields.get(idField))
                        .orElseThrow(() -> new IllegalArgumentException("No value found for ID field " + idField)));
            }
        }
        return contextType.idType().ofFields(idFieldValues);
    }

    public Context getContext(
            PatternConfig patternConfig,
            Map<String, ContextType> availableContextTypes,
            Map<IdField, Object> inheritedIdFields,
            ValueMapper mapper
    ) {
        // Determine the actual context type.
        ContextType contextType = Optional.ofNullable(availableContextTypes.get(contextTypeName))
                .orElseThrow(() -> new IllegalArgumentException("Context of type " + contextTypeName + " not part of allowed types " + availableContextTypes));

        return new Context(
                new ContextMetadata(
                        contextType,
                        contextType.idType() == IdType.NO_ID ? null : buildContextId(contextType, patternConfig, inheritedIdFields, mapper)
                ),
                mapper.mapToValue(
                        contextType.valueType(),
                        Optional.ofNullable(valuePattern)
                                .flatMap(patternConfig::evaluate)
                                .orElse(null)
                )
        );
    }
}
