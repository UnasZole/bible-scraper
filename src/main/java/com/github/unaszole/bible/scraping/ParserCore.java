package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;

import java.util.Deque;

/**
 * Logic specific to the parsing of a specific document structure.
 * @param <Position> The type representing a given position in the document.
 *                  (Typically a lexeme, in a lexeme-based parser)
 */
public interface ParserCore<Position> {
    class PositionParseOutput {
        /**
         * The context parsed from the given position in the given context stack, or null if none was found.
         */
        public final Context parsedContext;
        /**
         * False if the parser should request more contexts from the same requested position,
         * true if the parser should move to a next position.
         */
        public final boolean positionExhausted;

        public PositionParseOutput(Context parsedContext, boolean positionExhausted) {
            this.parsedContext = parsedContext;
            this.positionExhausted = positionExhausted;
        }

        public PositionParseOutput(Context parsedContext) {
            this(parsedContext, true);
        }
    }

    /**
     * @param ancestorStack The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
     * @param type The type of context to try creating from this position.
     * @param previousOfType Metadata of the previous sibling of the same type, or null if there is none.
     * @param position The position to check for a context opening.
     * @return The result of trying to build a context from this position.
     */
    PositionParseOutput readContext(Deque<ContextMetadata> ancestorStack, ContextType type,
                        ContextMetadata previousOfType, Position position);

    /**
     * Handle the position using external parsing logic if needed. That's useful if you are using a lexeme based parser,
     * but need to switch to a different type of lexeme or parser to analyse a given position.
     * When implemented, this method will usually break the positions in a sequence of smaller sub-positions of
     * a different type, and return a dedicated parser working on this sequence.
     *
     * @param position The position to parse with an external logic.
     * @param currentContextStack The current context stack when this position was reached. This stack may be modified during the external parsing.
     * @return The external parser to handle the position. If null, then the position will be handled by the current parser.
     */
    default Parser<?> parseExternally(Position position, Deque<Context> currentContextStack) {
        // No manual parsing is done by default.
        // This method should be overridden by implementations if some positions require manual parsing.
        return null;
    }
}
