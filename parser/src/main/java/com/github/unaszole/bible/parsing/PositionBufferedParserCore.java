package com.github.unaszole.bible.parsing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of {@link ParserCore} which allows outputting a fixed sequence of contexts from a position before
 * moving to the next.
 * @param <Position> The type representing a given position in the document.
 */
public abstract class PositionBufferedParserCore<Position> implements ParserCore<Position> {

    private static final Logger LOG = LoggerFactory.getLogger(PositionBufferedParserCore.class);

    public interface ContextReader {
        /**
         * Try to extract a context from the preset position.
         * @param ancestorStack The context stack where the returned context would be appended as child.
         * @param type The requested context type.
         * @param previousOfType The metadata of the previous sibling context of the requested type.
         * @return A context if one could be read (MUST be of the requested type), empty optional otherwise.
         */
        Optional<Context> readContext(List<Context> ancestorStack, ContextType type,
                                      ContextMetadata previousOfType);
    }

    protected abstract List<ContextReader> readContexts(List<Context> ancestorStack, ContextType type,
                                                  ContextMetadata previousOfType, Position position);

    private Position currentPosition = null;
    private final Deque<ContextReader> contextBuffer = new LinkedList<>();

    @Override
    public final PositionParseOutput readContext(List<Context> ancestorStack, ContextType type,
                                                 ContextMetadata previousOfType, Position position) {
        if(!contextBuffer.isEmpty() && !Objects.equals(currentPosition, position)) {
            LOG.warn("Parser moved to a new position {} while position {} still had buffered contexts : {}",
                    position, currentPosition, contextBuffer);
            contextBuffer.clear();
        }

        if(contextBuffer.isEmpty()) {
            // If buffer is empty, fill it with an actual call and save the position.
            contextBuffer.addAll(readContexts(ancestorStack, type, previousOfType, position));
            currentPosition = position;
        }

        if(contextBuffer.isEmpty()) {
            // If buffer is still empty, then this position has nothing to offer.
            return new PositionParseOutput(null, true);
        }

        Optional<Context> context = contextBuffer.peekFirst().readContext(ancestorStack, type, previousOfType);
        if(context.isPresent()) {
            assert context.get().metadata.type == type
                    : "Context " + context.get() + " must be of expected type " + type;
            // If the first context reader in buffer returned a context, the reader is consumed and the context is
            // returned.
            contextBuffer.removeFirst();
            return new PositionParseOutput(context.get(), contextBuffer.isEmpty());
        }

        // Finally, if the buffer has items but that don't match the requested type, the position is not exhausted.
        return new PositionParseOutput(null, false);
    }
}
