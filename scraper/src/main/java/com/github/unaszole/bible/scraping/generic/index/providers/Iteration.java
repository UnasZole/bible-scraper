package com.github.unaszole.bible.scraping.generic.index.providers;

import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.IdField;
import com.github.unaszole.bible.scraping.generic.PatternConfig;
import com.github.unaszole.bible.scraping.generic.ValueMapper;
import com.github.unaszole.bible.scraping.generic.index.IndexNode;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Iterate a container definition.
 * @param variable The iteration variable.
 * @param container The container definition.
 */
public record Iteration(
        @Nonnull IterationVariable variable,
        @Nonnull Container container
) implements IndexNodeProvider {
    public record IterationVariable(@Nonnull String name, int begin, int end) {
        public @Nonnull IntStream values() {
            return IntStream.range(begin, end + 1);
        }
    }

    @Override
    public @Nonnull List<IndexNode> getNodes(@Nonnull PatternConfig inheritedPatterns,
                                             @Nonnull Map<String, ContextType> availableContextTypes,
                                             @Nonnull Map<IdField, Object> inheritedIdFields,
                                             @Nonnull ValueMapper mapper) {
        // For all values of the iteration variable in order, evaluate the container with the iteration argument.
        return List.of(new IndexNode(
                variable.values()
                        .mapToObj(i -> inheritedPatterns.withArgument("$" + variable.name, Integer.toString(i)))
                        .flatMap(pc -> container.getNodes(pc, availableContextTypes, inheritedIdFields, mapper).stream())
                        .toList()
        ));
    }
}
