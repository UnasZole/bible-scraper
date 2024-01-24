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
	A parser processes a source document, broken down into a stream of lexemes (ie atomic significative elements),
	and populates a contextual tree out of it.
	The parser cannot "guess" what the document is about : a rootContext must always be provided along with the
	document stream, which describes what the document contains (A full bible ? A book ? A chapter ?).
	
	@param <Lexeme> The type of lexeme processed by this parser. For example, if the source document is an HTML page,
		the lexemes may be the HTML elements in document order.
*/
public abstract class Parser<Lexeme> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
	
	/**
		@param ancestorStack The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
		@param type The type of context to create from this lexeme.
		@param lexeme The lexeme to analyse.
		@return A new context extracted from the lexeme, or null if this lexeme does not create a context of the requested type.
	*/
	protected abstract Context readContext(Deque<ContextMetadata> ancestorStack, ContextType type, Lexeme lexeme);
	
	/**
		Handle the lexeme using external parsing logic. That's useful if the lexeme is in fact not atomic and may create many contexts.
		When implemented, this method will usually break the lexeme in smaller sub-lexemes and call the {@link #parse} method of a dedicated parser.
		
		@param lexeme The lexeme to parse with an external logic.
		@param currentContextStack The current context stack when this lexeme was reached. This stack may be modified during the external parsing.
	 	@param consumer A function that consumes context events in order, and returns instructions to continue or stop parsing.
		@return True if the lexeme was handled by external parsing (and thus should be ignored by this parser), false otherwise.
	*/
	protected boolean parseExternally(Lexeme lexeme, Deque<Context> currentContextStack, ContextConsumer consumer) {
		// No manual parsing is done by default.
		// This method should be overridden by implementations if some lexemes require manual parsing.
		return false;
	}

	/**
	 * Utility method for parsers : check if the current context is a descendant of a context of a given type.
	 * @param searchedAncestorType The type of ancestor to search for.
	 * @param ancestors The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
	 * @return True if an ancestor of the searched type is present, false otherwise.
	 */
	protected final boolean hasAncestor(ContextType searchedAncestorType, Deque<ContextMetadata> ancestors) {
		return ancestors.stream().anyMatch(a -> a.type == searchedAncestorType);
	}

	protected final boolean hasAncestorCtx(ContextType searchedAncestorType, Deque<Context> ancestors) {
		return ancestors.stream().anyMatch(a -> a.metadata.type == searchedAncestorType);
	}

	protected final boolean isInVerseText(Deque<ContextMetadata> ancestors) {
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

	/**
	 @param contextStack The current stack of the evaluation, where first element (top of the stack) is the closest existing ancestor.
	 @param lexeme The lexeme being processed.
	 */
	private Context getNextContextFrom(Deque<Context> contextStack, Lexeme lexeme) {
		Context closestAncestor = contextStack.peekFirst();

		// Look for a context that can be opened by this lexeme via an implicit path.
		final Context[] nextCtx = new Context[] { null };
		List<ContextMetadata> implicitPath = getImplicitPathToNext(
				contextStack.stream().map(c -> c.metadata).collect(Collectors.toCollection(LinkedList::new)),
				closestAncestor.getAllowedTypesForNextChild(),
				(stack, type) -> {
					// Try opening a context of the proposed type in the proposed stack location.
					Context next = readContext(stack, type, lexeme);
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
	
	private ContextConsumer.Instruction navigateToAppendPoint(Deque<Context> currentContextStack, ContextConsumer consumer) {
		Context currentContext = currentContextStack.peekFirst();

		// We're navigating to a new active context : consume its opening first.
		ContextConsumer.Instruction out = consumer.consume(ContextConsumer.EventType.OPEN, currentContext);
		if(out == ContextConsumer.Instruction.TERMINATE) {
			return out;
		}

		// Then check its children to navigate deeper.
		List<Context> children = currentContext.getChildren();
		if(children.isEmpty()) {
			// No child, just continue.
			return ContextConsumer.Instruction.CONTINUE;
		}
		else {
			// If there is more than one child, consume recursively all the previous ones.
			for(int i = 0; i < children.size() - 1; i++) {
				out = ContextConsumer.consumeAll(consumer, children.get(i));
				if(out == ContextConsumer.Instruction.TERMINATE) {
					return out;
				}
			}
			
			// Navigate to the last child and call recursively.
			currentContextStack.addFirst(children.get(children.size() - 1));
			return navigateToAppendPoint(currentContextStack, consumer);
		}
	}
	
	/**
		Parse a stream of lexemes to build a context tree, capturing contexts as they are completed.
		This method stops when the stream of lexemes is exhausted, and is designed to be called in a chain of parsers
		(ie by the parseExternally of another parser, hence the same method signature).
		For direct parsing, prefer one of its finishing alternatives, {@link #fill} or {@link #extract}.
		
		@param lexemes The stream of lexemes to parse.
		@param currentContextStack The stack of contexts we are in when starting the parse. Must contain at least one context.
			The deepest (last) context in that stack is the root context of the document.
		@param consumer A function that consumes context events in order, and returns instructions to continue or stop parsing.
	*/
	public final void parse(Stream<Lexeme> lexemes, Deque<Context> currentContextStack, ContextConsumer consumer) {
		Iterator<Lexeme> lexIt = lexemes.iterator();
		while(lexIt.hasNext()) {
			Lexeme lexeme = lexIt.next();
			
			if(!parseExternally(lexeme, currentContextStack, consumer))
			{
				// Check if this lexeme creates another context as descendant of the current.
				LinkedList<Context> nextContextStack = new LinkedList(currentContextStack);
				Context nextCtx = getNextContextFrom(nextContextStack, lexeme);
				// If not descendant of the current context, and if the current context may be considered complete, try to move up the stack.
				while(nextContextStack.size() > 1 && !nextContextStack.peekFirst().isIncomplete() && nextCtx == null) {
					nextContextStack.removeFirst();
					nextCtx = getNextContextFrom(nextContextStack, lexeme);
				}
				
				if(nextCtx != null) {
					// The lexeme created another context !
					
					// Move up the current context stack to align with the next context stack.
					while(currentContextStack.peekFirst() != nextContextStack.peekFirst()) {
						// Every item from the current stack that is not on the next stack is completed : send a close event.
						assert !currentContextStack.peekFirst().isIncomplete() : "Context to close " + currentContextStack.peekFirst() + " must be complete";
						if(consumer.consume(ContextConsumer.EventType.CLOSE, currentContextStack.removeFirst()) == ContextConsumer.Instruction.TERMINATE) {
							// If the instruction is to terminate, stop parsing here.
							return;
						}
					}
					
					// Finally, navigate to the current "append point" : this is the last child (of the last child of the last child, recursively),
					// of the newly added context, ie the place that may be extended by the next lexeme.
					// Consume all these newly added contexts on the way.
					if(navigateToAppendPoint(currentContextStack, consumer) == ContextConsumer.Instruction.TERMINATE) {
						// If the instruction is to terminate, stop parsing here.
						return;
					}
				}
				
				// No new context : lexeme was insignificant.
			}
		}
		
		// Stream is empty : no more parsing action in the core parsing logic.
		// Note that the context stack is left as-is, so another parser may take over.
		// Use parseAll if you want to the parser to close and consume the context stack at the end.
	}
	
	private void parseAll(Stream<Lexeme> lexemes, Deque<Context> currentContextStack, ContextConsumer consumer) {
		parse(lexemes, currentContextStack, consumer);
		while(!currentContextStack.isEmpty()) {
			if(ContextConsumer.Instruction.TERMINATE == consumer.consume(ContextConsumer.EventType.CLOSE, currentContextStack.removeFirst())) {
				return;
			}
		}
	}
	
	/**
		Fill a root context from a document.
		@param lexemes The stream of lexemes produced by the document.
		@param rootContext The root context of the document.
		@return The given rootContext, filled with all contents retrieved from the document.
	*/
	public final Context fill(Stream<Lexeme> lexemes, Context rootContext) {
		parseAll(lexemes, new LinkedList<>(List.of(rootContext)), ContextConsumer.PARSE_ALL);
		return rootContext;
	}
	
	/**
		Extract a wanted context from a document.
		@param lexemes The stream of lexemes produced by the document.
		@param rootContext The root context of the document.
		@param wantedContext The metadata of the context we wish to extract.
		@return The context matching the wantedContext metadata, containing all its children up to maxDepth.
	*/
	public final Context extract(Stream<Lexeme> lexemes, Context rootContext, ContextMetadata wantedContext) {
		ContextConsumer.Extractor extractor = new ContextConsumer.Extractor(wantedContext);
		parseAll(lexemes, new LinkedList<>(List.of(rootContext)), extractor);
		return extractor.getOutput();
	}
}