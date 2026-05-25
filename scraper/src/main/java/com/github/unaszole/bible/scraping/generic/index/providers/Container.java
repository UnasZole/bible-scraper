package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Container(
        @Nullable ContextProvider wrappingContextDefinition,
        @Nonnull PatternConfig patternConfig,
        @Nonnull List<? extends IndexNodeProvider> contents
) implements IndexNodeProvider {

    @Override
    public @Nonnull List<IndexNode> getNodes(@Nonnull PatternConfig inheritedPatterns,
                                             @Nonnull Map<String, ContextType> availableContextTypes,
                                             @Nonnull Map<IdField, Object> inheritedIdFields,
                                             @Nonnull ValueMapper mapper) {
        PatternConfig defaultedPatterns = patternConfig.defaultedBy(inheritedPatterns);

        Optional<Context> newContext = Optional.ofNullable(wrappingContextDefinition)
                .map(wcd -> wcd.getContext(defaultedPatterns, availableContextTypes, inheritedIdFields, mapper));
        Map<IdField, Object> newInheritedIdFields = new HashMap<>(inheritedIdFields);
        newContext.map(nc -> nc.metadata.id).ifPresent(ncid -> newInheritedIdFields.putAll(ncid.idMap));

        List<IndexNode> contentNodes = contents.stream()
                .flatMap(pp -> pp.getNodes(defaultedPatterns, availableContextTypes, newInheritedIdFields, mapper).stream())
                .toList();

        return List.of(new IndexNode(contentNodes, newContext.orElse(null)));
    }
}
