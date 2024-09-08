package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.*;
import com.github.unaszole.bible.stream.ContextEvent;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.stream.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parser processes a source document, broken down into a sequence of "positions".
 * The parser cannot "guess" what the document is about : an initial context stack (containing at least one root
 * context) must always be provided along with the document data, to describes what the data contains (A full bible ? A book ? A chapter ?).
 * @param <Position> The type of "position" processed by this parser.
*/
public class Parser<Position> implements Iterator<List<ContextEvent>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

	private final ParserCore<Position> core;
	private final Iterator<Position> positions;
	private final Deque<Context> currentContextStack;
	private Parser<?> currentExternalParser;
	private boolean closed = false;

	/**
	 *
	 * @param core A parser core that actually reads data from the source positions.
	 * @param positions An iterator on the positions in the document to parse.
	 * @param currentContextStack The context stack to parse against. Must at least contain one context.
	 *                               (Will be modified by the parsing).
	 */
	public Parser(ParserCore<Position> core, Iterator<Position> positions, Deque<Context> currentContextStack) {
		assert !currentContextStack.isEmpty();
		this.core = core;
		this.positions = positions;
        this.currentContextStack = currentContextStack;
		this.currentExternalParser = null;
    }

	/**
	 * Generate additional events once the parsing is complete.
	 * @return The sequence of events to append after the last position has been processed.
	 */
	protected List<ContextEvent> close(Deque<Context> currentContextStack) {
		// No closing is done by default.
		return List.of();
	}

	private static class ContextState {
		/**
		 * Stack of contexts in a given state.
		 */
		public final List<Context> contextStack;
		/**
		 * Sequence of events from the base state (in principle the last committed state) to this state.
		 */
		public final List<ContextEvent> events;
		/**
		 * If true, no more context can (or should) be extracted from the current position, parser needs to advance.
		 */
		public final boolean positionExhausted;

		private ContextState(List<Context> contextStack, List<ContextEvent> events, boolean positionExhausted) {
			this.contextStack =	Collections.unmodifiableList(contextStack);
			this.events = Collections.unmodifiableList(events);
			this.positionExhausted = positionExhausted;
		}

		public ContextState openChildContext(Context context) {
			assert contextStack.get(0).getAllowedTypesForNextChild().contains(context.metadata.type)
					: "Context to open " + context + " must be one of " + contextStack.get(0).getAllowedTypesForNextChild();

			// Head of stack is replaced by one where the new child is added.
			Context parentCtx = contextStack.get(0).addChild(context);
			LinkedList<Context> newStack = new LinkedList<>(contextStack);
			newStack.removeFirst();
			newStack.addFirst(parentCtx);

			// New child is added as head of stack, and open event is appended.
			newStack.addFirst(context);
			List<ContextEvent> newEvents = new LinkedList<>(events);
			newEvents.add(new ContextEvent(ContextEvent.Type.OPEN, context));

			return new ContextState(newStack, newEvents, positionExhausted);
		}

		public boolean canCloseCurrentContext() {
			// At least two contexts in stack (we must always keep a root), and head context is complete.
			return contextStack.size() > 1 && !contextStack.get(0).isIncomplete();
		}

		public ContextState closeCurrentContext() {
			assert canCloseCurrentContext()	: "Context to close " + contextStack.get(0) + " must be complete";

			// New state has the head context removed from stack, and a close event appended.
			List<Context> newStack = new LinkedList<>(contextStack);
			Context closed = newStack.remove(0);
			List<ContextEvent> newEvents = new LinkedList<>(events);
			newEvents.add(new ContextEvent(ContextEvent.Type.CLOSE, closed));

			return new ContextState(newStack, newEvents, positionExhausted);
		}

		public ContextState notifyPositionExhausted() {
			return new ContextState(contextStack, events, true);
		}
	}

	private ContextState parseDescendantContext(ContextState baseState, Position position) {
		Context baseContext = baseState.contextStack.get(0);

		// Loop through all allowed types for the next child of the head context.
		for(ContextType eltType: baseContext.getAllowedTypesForNextChild()) {
			ContextMetadata previousOfType = baseContext.getLastChildOfTypeMeta(eltType);

			// Try to extract a real context of this type.
			ParserCore.PositionParseOutput out = core.readContext(baseState.contextStack.stream()
							.map(c -> c.metadata)
							.collect(Collectors.toCollection(LinkedList::new)),
					eltType, previousOfType, position);

			if(out.parsedContext != null) {
				// Found a matching context : return a new state with it.
				ContextState resultState = baseState.openChildContext(out.parsedContext);
				if(out.positionExhausted) {
					resultState = resultState.notifyPositionExhausted();
				}
				return resultState;
			}

			Optional<ContextMetadata> implicitMeta = baseContext.metadata.getImplicitChildOfType(eltType, previousOfType);
			if(implicitMeta.isPresent() && eltType.valueType.implicitAllowed) {
				// If this element type can be created implicitly, build one and look recursively.

				// Build an implicit context for this element.
				Context newContext = new Context(implicitMeta.get(), eltType.valueType.implicitValue);

				// Call recursively until we get a real state.
				ContextState reachedState = parseDescendantContext(baseState.openChildContext(newContext), position);
				if(reachedState != null) {
					return reachedState;
				}
			}
		}

		// None of the allowed element types found a context, then do not return a new state.
		return null;
	}

	private List<ContextEvent> parsePosition(Position position) {
		// Initial state with current context stack and no event.
		ContextState committedState = new ContextState(new LinkedList<>(currentContextStack), new LinkedList<>(), false);

		// While new contexts can be opened from this position.
		while(!committedState.positionExhausted) {
			ContextState tentativeState = committedState;

			ContextState nextState;
			while((nextState = parseDescendantContext(tentativeState, position)) == null
					&& tentativeState.canCloseCurrentContext()) {
				// No descendant context could be opened : try to move up to the parent.
				tentativeState = tentativeState.closeCurrentContext();
			}

			if(nextState != null) {
				// We reached a valid next state : commit it !
				committedState = nextState;
			}
			else {
				// Couldn't reach a next state after testing all possibilities with the current position ?
				// Consider the position exhausted.
				committedState = committedState.notifyPositionExhausted();
			}
		}

		// Save the stack and return all events from the last committed state.
		this.currentContextStack.clear();
		this.currentContextStack.addAll(committedState.contextStack);
		return committedState.events;
	}

	/**
	 *
	 * @return True if this parser still has some unexplored positions to parse, false otherwise.
	 */
	@Override
	public final boolean hasNext() {
		if(currentExternalParser != null) {
			if(currentExternalParser.hasNext()) {
				// If there is an external parser with pending data, we delegate to it.
				return true;
			}
			else {
				// If it has no pending data, then we get rid of it, and proceed normally.
				currentExternalParser = null;
			}
		}
		return positions.hasNext() || !closed;
	}

	/**
	 * Advance the parser to the next meaningful position.
	 * @return The sequence of events built by the last parsed position. May be empty, if the last parsed position did
	 * not produce events (eg. if it set up an external parser, or if it reached the end of document).
	 * Nevertheless, even if an empty list is returned, it does not mean the parsing is complete : always call
	 * {@link #hasNext()} to know if there is more data to parse.
	 */
	@Override
	public final List<ContextEvent> next() {
		if(currentExternalParser != null) {
			// If there is an external parser, we delegate to it.
			return currentExternalParser.next();
		}

		while(positions.hasNext()) {
			Position position = positions.next();

			// Check if we need external parsing logic at this position.
			this.currentExternalParser = core.parseExternally(position, currentContextStack);
			if(currentExternalParser != null) {
				// We built an external parser.
				// Return an empty list, and the next iteration will consume it.
				return List.of();
			}

			List<ContextEvent> events = parsePosition(position);
			if(!events.isEmpty()) {
				// Events produced from this position : return them.
				return events;
			}
			// No new events were produced from this position : move to next position.
		}

		// Reached the last position.
		this.closed = true;
		return close(currentContextStack);
	}

	public final Stream<ContextEvent> asEventStream() {
		return StreamUtils.toStream(StreamUtils.toFlatIterator(this));
	}

	public static class TerminalParser<Position> extends Parser<Position> {

		private final Context rootContext;

		/**
		 * @param positions An iterator on the positions in the document to parse.
		 * @param rootContext The root context to be filled by this parser.
		 */
		public TerminalParser(ParserCore<Position> core, Iterator<Position> positions, Context rootContext) {
			super(core, positions, new LinkedList<>(List.of(rootContext)));
			this.rootContext = rootContext;
		}

		@Override
		protected List<ContextEvent> close(Deque<Context> currentContextStack) {
			// Collect close events for the active stack, up until the root context
			// (excluded, since it was not opened by the parser itself).
			List<ContextEvent> events = new ArrayList<>();
			while (currentContextStack.peekFirst().contextUniqueId != rootContext.contextUniqueId) {
				events.add(new ContextEvent(ContextEvent.Type.CLOSE, currentContextStack.removeFirst()));
			}
			return events;
		}

		public ContextStream.Single asContextStream() {
			return new ContextStream.Single(rootContext.metadata, StreamUtils.concatStreams(
					Stream.of(new ContextEvent(ContextEvent.Type.OPEN, rootContext)),
					asEventStream(),
					Stream.of(new ContextEvent(ContextEvent.Type.CLOSE, rootContext))
			));
		}

		/**
		 * Process the whole document at once and fill the root context that was given at construction time.
		 */
		public final void fill() {
			while(hasNext()) {
				next();
			}
		}
	}
}