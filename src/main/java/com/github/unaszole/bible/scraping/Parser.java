package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parser processes a source document, broken down into a sequence of "positions".
 * The parser cannot "guess" what the document is about : an initial context stack (containing at least one root
 * context) must always be provided along with the document data, to describes what the data contains (A full bible ? A book ? A chapter ?).
 * @param <Position> The type of "position" processed by this parser.
*/
public abstract class Parser<Position> implements Iterator<List<ContextEvent>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

	/**
	 * Utility method for parsers : check if the current context is a descendant of a context of a given type.
	 * @param searchedAncestorType The type of ancestor to search for.
	 * @param ancestors The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
	 * @return True if an ancestor of the searched type is present, false otherwise.
	 */
	protected static boolean hasAncestor(ContextType searchedAncestorType, Deque<ContextMetadata> ancestors) {
		return ancestors.stream().anyMatch(a -> a.type == searchedAncestorType);
	}

	protected static boolean hasAncestorCtx(ContextType searchedAncestorType, Deque<Context> ancestors) {
		return ancestors.stream().anyMatch(a -> a.metadata.type == searchedAncestorType);
	}

	protected static boolean isInVerseText(Deque<ContextMetadata> ancestors) {
		return hasAncestor(ContextType.VERSE, ancestors) && hasAncestor(ContextType.FLAT_TEXT, ancestors);
	}
	
	private static void integrateNewContext(Context existingAncestor, List<ContextMetadata> implicitAncestors, Context newContext) {
		Context currentAncestor = existingAncestor;
		for(ContextMetadata implicitAncestor: implicitAncestors) {
			Context newAncestor = new Context(implicitAncestor);
			currentAncestor.addChild(newAncestor);
			currentAncestor = newAncestor;
		}
		
		currentAncestor.addChild(newContext);
	}

	/**
	 *
	 * @param contextStack The current stack of the evaluation, with possible implicit contexts appended.
	 *                     First element (top of the stack) is the direct parent of the element being checked.
	 * @param allowedEltTypes The type of elements allowed at this point in the stack.
	 * @param isTypeOkForNext Returns true if an element at the given stack position and of the given type is accepted as end of the path.
	 * @return The list of implicit contexts built to reach the accepted element type (not including this accepted element type).
	 */
	private static List<ContextMetadata> getImplicitPathToNext(Deque<ContextMetadata> contextStack,
													  Set<ContextType> allowedEltTypes,
													  BiPredicate<Deque<ContextMetadata>, ContextType> isTypeOkForNext) {
		for(ContextType eltType: allowedEltTypes) {
			if(isTypeOkForNext.test(contextStack, eltType)) {
				// If this element type is accepted at the current stack location, return an empty list.
				// (No additional implicit element needed.)
				return List.of();
			}

			if(eltType.implicitAllowed) {
				// If this element type can be created implicitly, look for an implicit path from it.

				// Build an implicit context for this element.
				ContextMetadata eltMeta = ContextMetadata.fromParent(eltType, contextStack.peekFirst());

				// Build a new context stack, with an implicit context for this element on top.
				Deque<ContextMetadata> contextStackWithElt = new LinkedList<>(contextStack);
				contextStackWithElt.addFirst(eltMeta);

				// Recursively search for an implicit path from this element's children.
				List<ContextMetadata> implicitFromThisElt = getImplicitPathToNext(contextStackWithElt,
						eltType.getAllowedTypesForFirstChild(), isTypeOkForNext);

				if(implicitFromThisElt != null) {
					// If any found, then return the path with this implicit element and the found path from it.
					List<ContextMetadata> result = new ArrayList<>();
					result.add(eltMeta);
					result.addAll(implicitFromThisElt);
					return result;
				}
			}
		}

		// None of the allowed element types provided an implicit path ? Then no implicit path available.
		return null;
	}

	public static boolean addDescendant(Context rootContext, Context descendant) {
		Set<ContextType> allowedTypes = rootContext.getAllowedTypesForNextChild();

		if(allowedTypes.contains(descendant.metadata.type)) {
			// The root context can contain the descendant directly : add it.
			rootContext.addChild(descendant);
			return true;
		}

		Optional<Context> lastChild = rootContext.getLastChild();
		if(lastChild.isPresent()) {
			// The root context has a last child : check recursively if it can accept the descendant.
			if(addDescendant(lastChild.get(), descendant)) {
				return true;
			}
		}

		// Finally, check if an implicit path is possible.
		// Build a "fake" context stack from this root, as the stack is not used by the predicate anyway.
		Deque<ContextMetadata> stack = new LinkedList<>();
		stack.addFirst(rootContext.metadata);
		List<ContextMetadata> implicitPath = getImplicitPathToNext(stack, allowedTypes,
				(s, type) -> type == descendant.metadata.type);

		if(implicitPath != null) {
			// If an implicit path is possible, integrate the descendant to the root via this path.
			integrateNewContext(rootContext, implicitPath, descendant);
			return true;
		}

		// Could not add the descendant to the given root.
		return false;
	}

	/**
	 * Utility method for parser implementations to build a deep context, ie a context containing others.
	 * Contrary to the Context constructor, which only allows specifying direct children, this method accepts further
	 * descendants by building implicit ancestors if needed.
	 * This makes the code calling this method a lot less sensitive to evolutions of the context structure
	 * (ie enriching the context tree with new implicit elements) than if it was using the Context constructor.
	 *
	 * @param metadata The metadata of the context to build.
	 * @param value The value stored in the context, if any.
	 * @param descendants The sequence of descendants to append to the built context, in order.
	 * @return The new context, containing the descendants.
	 */
	public static Context buildContext(ContextMetadata metadata, String value, Context... descendants) {
		Context newContext = new Context(metadata, value);
		for(Context descendant: descendants) {
			if(!addDescendant(newContext, descendant)) {
				throw new IllegalArgumentException("Cannot insert " + descendant + " as descendant of " + newContext);
			}
		}
		return newContext;
	}

	/**
	 * Equivalent to {@link #buildContext(ContextMetadata, String, Context...)}, with a null value.
	 * @param metadata The metadata of the context to build.
	 * @param descendants The sequence of descendants to append to the built context, in order.
	 * @return The new context, containing the descendants.
	 */
	public static Context buildContext(ContextMetadata metadata, Context... descendants) {
		return buildContext(metadata, null, descendants);
	}
	
	private static void navigateToAppendPoint(Deque<Context> currentContextStack, Context newContext, List<ContextEvent> events) {
		Context currentContext = currentContextStack.peekFirst();

		// Then check its children to navigate deeper.
		List<Context> children = currentContext.getChildren();
		if(!children.isEmpty()) {
			if(newContext == null || newContext == currentContext) {
				// If we are under the new context, all previous children are also new and need to be collected.
				for(int i = 0; i < children.size() - 1; i++) {
					events.addAll(ContextEvent.fromContext(children.get(i)));
				}

				// Set new context to null for recursive iterations, to let them know we are under the new context.
				newContext = null;
			}

			// Navigate to the last child (newly created, so build an event) and call recursively.
			Context lastChild = children.get(children.size() - 1);
			currentContextStack.addFirst(lastChild);
			events.add(new ContextEvent(ContextEvent.Type.OPEN, lastChild));
			navigateToAppendPoint(currentContextStack, newContext, events);
		}
	}

	private final Iterator<Position> positions;
	private final Deque<Context> currentContextStack;
	private Parser<?> currentExternalParser;

	/**
	 *
	 * @param positions An iterator on the positions in the document to parse.
	 * @param currentContextStack The context stack to parse against. Must at least contain one context.
	 *                               (Will be modified by the parsing).
	 */
	protected Parser(Iterator<Position> positions, Deque<Context> currentContextStack) {
		assert !currentContextStack.isEmpty();
		this.positions = positions;
        this.currentContextStack = currentContextStack;
		this.currentExternalParser = null;
    }

	/**
	 * @param ancestorStack The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
	 * @param type The type of context to try creating from this position.
	 * @param position The position to check for a context opening.
	 * @return A new context built from this position, or null if no context can be created at this position.
	 */
	protected abstract Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type, Position position);

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
	protected Parser<?> parseExternally(Position position, Deque<Context> currentContextStack) {
		// No manual parsing is done by default.
		// This method should be overridden by implementations if some positions require manual parsing.
		return null;
	}

	/**
	 * Generate additional events once the parsing is complete.
	 * @return The sequence of events to append after the last position has been processed.
	 */
	protected List<ContextEvent> close(Deque<Context> currentContextStack) {
		// No closing is done by default.
		return List.of();
	}

	/**
	 @param contextStack The current stack of the evaluation, where first element (top of the stack) is the closest existing ancestor.
	 @param position The lexeme being processed.
	 */
	private Context getNextContextFrom(Deque<Context> contextStack, Position position) {
		Context closestAncestor = contextStack.peekFirst();

		// Look for a context that can be opened by this lexeme via an implicit path.
		final Context[] nextCtx = new Context[] { null };
		List<ContextMetadata> implicitPath = getImplicitPathToNext(
				contextStack.stream().map(c -> c.metadata).collect(Collectors.toCollection(LinkedList::new)),
				closestAncestor.getAllowedTypesForNextChild(),
				(stack, type) -> {
					// Try opening a context of the proposed type in the proposed stack location.
					Context next = readContext(stack, type, position);
					if(next != null) {
						// If successful, save it and validate this implicit path.
						nextCtx[0] = next;
						return true;
					}
					return false;
				}
		);

		if(nextCtx[0] != null) {
			// If a next context was actually built, integrate it as descendant of the closest ancestor.
			integrateNewContext(closestAncestor, implicitPath, nextCtx[0]);
		}
		return nextCtx[0];
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
		return positions.hasNext();
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
			this.currentExternalParser = parseExternally(position, currentContextStack);
			if(currentExternalParser != null) {
				// We built an external parser.
				// Return an empty list, and the next iteration will consume it.
				return List.of();
			}

			// Check if another context can be created at this position as descendant of the current.
			LinkedList<Context> nextContextStack = new LinkedList<>(currentContextStack);
			Context nextCtx = getNextContextFrom(nextContextStack, position);
			// If not descendant of the current context, and if the current context may be considered complete, try to move up the stack.
			while(nextContextStack.size() > 1 && !nextContextStack.peekFirst().isIncomplete() && nextCtx == null) {
				nextContextStack.removeFirst();
				nextCtx = getNextContextFrom(nextContextStack, position);
			}

			if(nextCtx != null) {
				// Another context was created at this position !
				// Collect related events.
				List<ContextEvent> events = new ArrayList<>();

				// Move up the current context stack to align with the next context stack.
				while(currentContextStack.peekFirst() != nextContextStack.peekFirst()) {
					// Every item from the current stack that is not on the next stack is completed : collect a close event.
					assert !currentContextStack.peekFirst().isIncomplete() : "Context to close " + currentContextStack.peekFirst() + " must be complete";
					events.add(new ContextEvent(ContextEvent.Type.CLOSE, currentContextStack.removeFirst()));
				}

				// Finally, navigate to the current "append point" : this is the last child (of the last child of the last child, recursively),
				// of the newly added context, ie the place that may be extended by the next lexeme.
				// Collect events for all newly added contexts on the way.
				navigateToAppendPoint(currentContextStack, nextCtx, events);

				// Return these events and stop at this position until next call.
				return events;
			}

			// No new context could be opened at this position : move to next position.
		}

		// Reached the last position.
		return close(currentContextStack);
	}

	public final Stream<ContextEvent> asEventStream() {
		return ParsingUtils.toStream(ParsingUtils.toFlatIterator(this));
	}

	public static abstract class TerminalParser<Position> extends Parser<Position> {

		private final Context rootContext;

		/**
		 * @param positions An iterator on the positions in the document to parse.
		 * @param rootContext The root context to be filled by this parser.
		 */
		protected TerminalParser(Iterator<Position> positions, Context rootContext) {
			super(positions, new LinkedList<>(List.of(rootContext)));
			this.rootContext = rootContext;
		}

		@Override
		protected List<ContextEvent> close(Deque<Context> currentContextStack) {
			// Collect close events for the active stack, up until the root context
			// (excluded, since it was not opened by the parser itself).
			List<ContextEvent> events = new ArrayList<>();
			while (!Objects.equals(currentContextStack.peekFirst(), rootContext)) {
				events.add(new ContextEvent(ContextEvent.Type.CLOSE, currentContextStack.removeFirst()));
			}
			return events;
		}

		/**
		 * Process the whole document at once and fill the root context that was given at construction time.
		 */
		public final void fill() {
			while(hasNext()) {
				next();
			}
		}

		/**
		 * Extract a wanted context from the parsed document.
		 * @param wantedContext The metadata of the context we wish to extract.
		 * @return The context matching the wantedContext metadata.
		 */
		public final Context extract(ContextMetadata wantedContext) {
			while(hasNext()) {
				for(ContextEvent event: next()) {
					if(event.type == ContextEvent.Type.CLOSE && Objects.equals(event.context.metadata, wantedContext)) {
						return event.context;
					}
				}
			}
			return null;
		}
	}
}