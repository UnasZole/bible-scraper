package com.github.unaszole.bible.datamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;

public class ContextSequence {

	private static final Logger LOG = LoggerFactory.getLogger(ContextSequence.class);
	
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
	
	public final Set<ContextType> allowedTypes;
	public final int minOccurrences;
	public final int maxOccurrences;
	
	public ContextSequence(ContextType[] allowedTypes, int minOccurrences, int maxOccurrences) {
		this.allowedTypes = Set.of(allowedTypes);
		this.minOccurrences = minOccurrences;
		this.maxOccurrences = maxOccurrences;
	}
	
	public static enum Status {
		INCOMPLETE, // Requires at least one new element.
		OPEN, // Can accept at least one new element.
		CLOSED // Cannot accept any new element.
	}
	
	/**
		@param contexts A queue of context types to match. Each context that match will be unqueued.
		@return The status of this sequence after consuming the given contexts.
	*/
	public Status consume(Queue<ContextType> contexts) {
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
			return Status.CLOSED;
		}

		if(consumed < minOccurrences) {
			return Status.INCOMPLETE;
		}
		if(consumed >= maxOccurrences) {
			return Status.CLOSED;
		}
		return Status.OPEN;
	}
}