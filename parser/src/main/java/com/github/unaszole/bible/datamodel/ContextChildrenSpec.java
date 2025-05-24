package com.github.unaszole.bible.datamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ContextChildrenSpec {
    private static final Logger LOG = LoggerFactory.getLogger(com.github.unaszole.bible.datamodel.ContextChildrenSpec.class);

    public static class ContextSequence {

        public static ContextSequence atLeastOne(ContextType... types) {
            return new ContextSequence(types, 1, Integer.MAX_VALUE);
        }

        public static ContextSequence one(ContextType... types) {
            return new ContextSequence(types, 1, 1);
        }

        public static ContextSequence atMostOne(ContextType... types) {
            return new ContextSequence(types, 0, 1);
        }

        public static ContextSequence any(ContextType... types) {
            return new ContextSequence(types, 0, Integer.MAX_VALUE);
        }

        public final List<ContextType> allowedTypes;
        public final int minOccurrences;
        public final int maxOccurrences;

        /**
         *
         * @param allowedTypes A set of allowed context types in this sequence, ordered by match priority : the parser will
         *                     attempt matching the first type first, etc.
         *                     Contexts that can be opened implicitly should be defined last (so that explicit contexts have
         *                     priority).
         * @param minOccurrences Minimum number of occurrences to consider the sequence complete.
         * @param maxOccurrences Maximum number of occurrences after which the sequence should be closed.
         */
        public ContextSequence(ContextType[] allowedTypes, int minOccurrences, int maxOccurrences) {
            this.allowedTypes = List.of(allowedTypes);
            this.minOccurrences = minOccurrences;
            this.maxOccurrences = maxOccurrences;
        }

        public enum Status {
            INCOMPLETE, // Requires at least one new element.
            OPEN, // Can accept at least one new element.
            CLOSED // Cannot accept any new element.
        }

        /**
         @param contexts A queue of context types to match. Each context that match will be unqueued.
         @return The status of this sequence after consuming the given contexts.
         */
        public ContextSequence.Status consume(Queue<ContextType> contexts) {
            int consumed = 0;

            while(consumed < maxOccurrences && !contexts.isEmpty() && allowedTypes.contains(contexts.peek())) {
                contexts.poll();
                consumed++;
            }

            if(!contexts.isEmpty()) {
                // We have unmatched contexts, we must close the sequence.
                if(consumed < minOccurrences)
                {
                    // But if the sequence is incomplete, it's a structure error !
                    LOG.error("Sequence {} is closed while incomplete after meeting forbidden element {}",
                            this, contexts.peek()
                    );
                }
                return ContextSequence.Status.CLOSED;
            }

            if(consumed < minOccurrences) {
                return ContextSequence.Status.INCOMPLETE;
            }
            if(consumed >= maxOccurrences) {
                return ContextSequence.Status.CLOSED;
            }
            return ContextSequence.Status.OPEN;
        }
    }
    
    private final ContextSequence[] allowedChildren;

    public ContextChildrenSpec(ContextSequence[] allowedChildren) {
        this.allowedChildren = allowedChildren;
    }

    public List<ContextType> getAllowedTypesForNextChild(List<ContextType> currentChildrenTypes) {
        List<ContextType> allowedTypes = new ArrayList<>();

        // Add all children to a queue.
        ArrayDeque<ContextType> childrenQueue = new ArrayDeque<>(currentChildrenTypes);

        for(ContextSequence childrenSequence: allowedChildren) {
            // Iterate through all allowed children sequences.

            // Consume as many children as this sequence accepts.
            switch(childrenSequence.consume(childrenQueue)) {
                case INCOMPLETE:
                    // This sequence is incomplete. Next child can either extend a previous sequence, or fill this one, but nothing further.
                    // Return immediately.
                    allowedTypes.addAll(childrenSequence.allowedTypes);
                    return allowedTypes;
                case OPEN:
                    // This sequence is open : this sequence's types are accepted, but a next sequence might be allowed as well.
                    // Keep iterating.
                    allowedTypes.addAll(childrenSequence.allowedTypes);
                    break;
                case CLOSED:
                    // This sequence is closed : it does not contribute to the allowed types.
                    // Keep iterating.
                    break;
            }
        }

        return allowedTypes;
    }

    public boolean isIncomplete(List<ContextType> currentChildrenTypes) {
        // Add all children to a queue.
        ArrayDeque<ContextType> childrenQueue = new ArrayDeque<>(currentChildrenTypes);

        for(ContextSequence childrenSequence: allowedChildren) {
            // Iterate through all allowed children sequences.

            // Consume as many children as this sequence accepts.
            switch(childrenSequence.consume(childrenQueue)) {
                case INCOMPLETE:
                    // This sequence is incomplete, so the full context is incomplete.
                    return true;
                default:
                    // Sequence can be considered complete, just move to the next.
                    break;
            }
        }

        // All children sequences are complete.
        return false;
    }
}
