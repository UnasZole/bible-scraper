package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.downloading.HttpSourceFile;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.data.PageData;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;

public record PatternPage(
        @Nonnull PatternConfig patternConfig
) implements IndexNodeProvider {
    @Override
    public @Nonnull List<IndexNode> getNodes(@Nonnull PatternConfig inheritedPatterns,
                                             @Nonnull Map<String, ContextType> availableContextTypes,
                                             @Nonnull Map<IdField, Object> inheritedIdFields,
                                             @Nonnull ValueMapper mapper) {
        PatternConfig defaultedPatterns = patternConfig.defaultedBy(inheritedPatterns);
        return new HttpSourceFile.Builder().buildFrom(defaultedPatterns::evaluate)
                .map(sf -> new PageData(
                        sf,
                        defaultedPatterns.arguments(),
                        defaultedPatterns.evaluate("Parser").orElse("main")
                ))
                .map(IndexNode::new)
                .stream().toList();
    }
}
