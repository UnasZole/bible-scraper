package com.github.unaszole.bible.parsing;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

public class WrappingParserCore extends PositionBufferedParserCore<WrappingParserCore.Position> {

    public enum Position { BEFORE, INSIDE, AFTER }

    private final Function<Deque<Context>, Parser<?>> wrappedParserBuilder;
    private final List<ContextReader> contextsBefore;
    private final List<ContextReader> contextsAfter;

    public WrappingParserCore(Function<Deque<Context>, Parser<?>> wrappedParserBuilder,
                              List<ContextReader> contextsBefore,
                              List<ContextReader> contextsAfter) {
        this.wrappedParserBuilder = wrappedParserBuilder;
        this.contextsBefore = contextsBefore;
        this.contextsAfter = contextsAfter;
    }

    @Override
    public Parser<?> parseExternally(Position position, Deque<Context> currentContextStack) {
        return position == Position.INSIDE ? wrappedParserBuilder.apply(currentContextStack) : null;
    }

    @Override
    protected List<ContextReader> readContexts(Deque<ContextMetadata> ancestorStack, ContextType type,
                                               ContextMetadata previousOfType, Position position) {
        switch (position) {
            case BEFORE:
                return contextsBefore;
            case AFTER:
                return contextsAfter;
            default:
                throw new IllegalArgumentException("Illegal position for wrapped parser " + position);
        }
    }

    public static Parser<?> wrap(Function<Deque<Context>, Parser<?>> wrappedParserBuilder,
                                 List<ContextReader> contextsBefore,
                                 List<ContextReader> contextsAfter,
                                 Deque<Context> currentContextStack) {
        return new Parser<>(
                new WrappingParserCore(wrappedParserBuilder, contextsBefore, contextsAfter),
                Arrays.stream(Position.values()).iterator(),
                currentContextStack
        );
    }
}
