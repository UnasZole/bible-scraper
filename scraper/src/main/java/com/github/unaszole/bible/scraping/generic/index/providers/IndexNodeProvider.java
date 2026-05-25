package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;

public interface IndexNodeProvider {
    @Nonnull List<IndexNode> getNodes(@Nonnull PatternConfig inheritedPatterns,
                                      @Nonnull Map<String, ContextType> availableContextTypes,
                                      @Nonnull Map<IdField, Object> inheritedIdFields,
                                      @Nonnull ValueMapper mapper);
}
