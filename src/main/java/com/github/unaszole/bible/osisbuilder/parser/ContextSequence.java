package com.github.unaszole.bible.osisbuilder.parser;

import java.util.Queue;
import java.util.Set;

public class ContextSequence {
	
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
		
		if(consumed < minOccurrences) {
			return Status.INCOMPLETE;
		}
		if(consumed >= maxOccurrences) {
			return Status.CLOSED;
		}
		return Status.OPEN;
	}
}