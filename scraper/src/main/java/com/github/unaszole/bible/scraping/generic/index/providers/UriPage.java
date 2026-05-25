package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.downloading.HttpSourceFile;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.data.PageData;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import jakarta.annotation.Nonnull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public record UriPage(@Nonnull PatternConfig patternConfig, @Nonnull URI uri) implements IndexNodeProvider {
    @Override
    public @Nonnull List<IndexNode> getNodes(@Nonnull PatternConfig inheritedPatterns,
                                             @Nonnull Map<String, ContextType> availableContextTypes,
                                             @Nonnull Map<IdField, Object> inheritedIdFields,
                                             @Nonnull ValueMapper mapper) {
        try {
            PatternConfig defaultedPatterns = patternConfig.defaultedBy(inheritedPatterns);
            return List.of(new IndexNode(new PageData(
                    new HttpSourceFile(uri.toURL()),
                    defaultedPatterns.arguments(),
                    defaultedPatterns.evaluate("Parser").orElse("main")
            )));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
