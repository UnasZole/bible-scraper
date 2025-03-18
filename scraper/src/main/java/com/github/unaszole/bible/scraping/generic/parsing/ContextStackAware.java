package com.github.unaszole.bible.scraping.generic.parsing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;

import java.util.Collection;
import java.util.List;

/**
 * An item of the parsing configuration that can trigger only at some points in the context stack.
 */
public abstract class ContextStackAware {
    /**
     * A required ancestor type for this extractor to trigger.
     */
    public List<ContextType> withAncestors;
    /**
     * An excluded ancestor type for this extractor to trigger.
     */
    public List<ContextType> withoutAncestors;

    protected boolean isContextStackValid(Collection<ContextMetadata> ancestorStack) {
        return
                (this.withAncestors == null || this.withAncestors.stream().allMatch(
                        t -> ancestorStack.stream().anyMatch(a -> a.type == t)
                )) &&
                (this.withoutAncestors == null || ancestorStack.stream().noneMatch(
                        a -> this.withoutAncestors.contains(a.type)
                ));
    }
}
