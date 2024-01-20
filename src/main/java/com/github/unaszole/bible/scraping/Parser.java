package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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
		@param maxDepth The deepest type of context we wish to parse. Descendants of these contexts will be ignored.
		@param capture A function that consumes all completed contexts in order. Returns true if the parsing should be stopped.
		@return True if the lexeme was handled by external parsing (and thus should be ignored by this parser), false otherwise.
	*/
	protected boolean parseExternally(Lexeme lexeme, Deque<Context> currentContextStack, ContextType maxDepth, Predicate<Context> capture) {
		// No manual parsing is done by default.
		// This method should be overridden by implementations if some lexemes require manual parsing.
		return false;
	}

	/**
	 * Utility method for parsers : check if the current context is located under a context of a given type.
	 * @param searchedAncestorType The type of ancestor to search for.
	 * @param ancestors The metadata of the ancestor contexts, potentially implicit (first element is the direct parent).
	 * @return True if an ancestor of the searched type is present, false otherwise.
	 */
	protected final boolean isUnderA(ContextType searchedAncestorType, Deque<ContextMetadata> ancestors) {
		return ancestors.stream().anyMatch(a -> a.type == searchedAncestorType);
	}

	protected final boolean isInVerseText(Deque<ContextMetadata> ancestors) {
		return isUnderA(ContextType.VERSE, ancestors) &&
				!isUnderA(ContextType.MAJOR_SECTION_TITLE, ancestors) &&
				!isUnderA(ContextType.SECTION_TITLE, ancestors);
	}
	
	private void integrateNewContext(Context existingAncestor, List<ContextMetadata> implicitAncestors, Context newContext) {
		Context currentAncestor = existingAncestor;
		for(ContextMetadata implicitAncestor: implicitAncestors) {
			Context newAncestor = new Context(implicitAncestor);
			currentAncestor.addChild(newAncestor);
			currentAncestor = newAncestor;
		}
		
		currentAncestor.addChild(newContext);
	}
	
	private <E> List<E> append(List<E> originalList, E newElt) {
		List<E> outList = new ArrayList<>(originalList);
		outList.add(newElt);
		return outList;
	}
	
	private Deque<ContextMetadata> buildAncestorStack(Deque<Context> contextStack, List<ContextMetadata> implicitAncestors) {
		Deque<ContextMetadata> outList = new LinkedList<>();
		
		outList.addAll(contextStack.stream().map(c -> c.metadata).collect(Collectors.toList()));
		
		for(ContextMetadata implicitAncestor: implicitAncestors) {
			outList.addFirst(implicitAncestor);
		}
		
		return outList;
	}
	
	/**
		@param contextStack The current stack of the evaluation, where first element (top of the stack) is the closest existing ancestor.
		@param implicitAncestors The list of implicit contexts ready to be created, where fist element is a child of the contextStack top,
			and last element is the closest implicit ancestor.
		@param allowedTypes The allowed types for the context at this point.
		@param lexeme The lexeme being processed.
		@param maxDepth The deepest type of context to explore.
	*/
	private Context getNextContextFrom(Deque<Context> contextStack, List<ContextMetadata> implicitAncestors,
		Set<ContextType> allowedTypes, Lexeme lexeme, ContextType maxDepth) {
		Context closestAncestor = contextStack.peekFirst();
		ContextType parent = implicitAncestors.isEmpty() ? closestAncestor.metadata.type : implicitAncestors.get(implicitAncestors.size() - 1).type;
			
		if(parent == maxDepth) {
			// Parent is the max depth : we don't look at possible child contexts.
			return null;
		}
		
		Deque<ContextMetadata> ancestorStack = buildAncestorStack(contextStack, implicitAncestors);
		
		// Check all allowed types for next child.
		for(ContextType childType: allowedTypes) {
			// Check if the lexeme can build a context of this type.
			Context nextCtx = readContext(ancestorStack, childType, lexeme);
			
			if(nextCtx != null) {
				// We built a new context from the lexeme !
				
				assert childType == nextCtx.metadata.type : "Parsed context type " + nextCtx.metadata.type + " must match expected " + childType;
				
				// Integrate it with the existing ancestor and return it.
				integrateNewContext(closestAncestor, implicitAncestors, nextCtx);
				return nextCtx;
			}
			else if(childType.implicitAllowed) {
				// Else, if this type can be created implicitly, look recursively if the lexeme opens its first child.
				nextCtx = getNextContextFrom(contextStack,
					append(implicitAncestors, ContextMetadata.fromParent(childType, closestAncestor.metadata)),
					childType.getAllowedTypesForFirstChild(), lexeme, maxDepth);
				
				if(nextCtx != null) {
					// If a new context was returned recursively, it's already integrated. Return it directly.
					return nextCtx;
				}
			}
		}
		return null;
	}
	
	private Context getNextContextFrom(Deque<Context> contextStack, Lexeme lexeme, ContextType maxDepth) {
		Context parent = contextStack.peekFirst();
		
		return getNextContextFrom(contextStack, List.of(), parent.getAllowedTypesForNextChild(),
			lexeme, maxDepth);
	}
	
	private void navigateToAppendPoint(Deque<Context> currentContextStack, Predicate<Context> capture) {
		Context currentContext = currentContextStack.peekFirst();
		
		List<Context> children = currentContext.getChildren();
		if(children.isEmpty()) {
			return;
		}
		else {
			if(children.size() > 1) {
				// If there is more than one child, capture recursively all the previous ones.
				// TODO.
			}
			
			// Navigate to the last child and call recursively.
			currentContextStack.addFirst(children.get(children.size() - 1));
			navigateToAppendPoint(currentContextStack, capture);
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
		@param maxDepth The deepest type of context we wish to parse. Descendants of these contexts will be ignored.
		@param capture A function that consumes all completed contexts in order. Returns true if the parsing should be stopped.
	*/
	public final void parse(Stream<Lexeme> lexemes, Deque<Context> currentContextStack, ContextType maxDepth, Predicate<Context> capture) {
		Iterator<Lexeme> lexIt = lexemes.iterator();
		while(lexIt.hasNext()) {
			Lexeme lexeme = lexIt.next();
			
			if(!parseExternally(lexeme, currentContextStack, maxDepth, capture))
			{
				// Check if this lexeme creates another context, either as descendant of the current.
				LinkedList<Context> nextContextStack = new LinkedList(currentContextStack);
				Context nextCtx = getNextContextFrom(nextContextStack, lexeme, maxDepth);
				// If not descendant of the current context, and if the current context may be considered complete, try to move up the stack.
				while(nextContextStack.size() > 1 && !nextContextStack.peekFirst().isIncomplete() && nextCtx == null) {
					nextContextStack.removeFirst();
					nextCtx = getNextContextFrom(nextContextStack, lexeme, maxDepth);
				}
				
				if(nextCtx != null) {
					// The lexeme created another context !
					
					// Move up the current context stack to align with the next context stack.
					while(currentContextStack.peekFirst() != nextContextStack.peekFirst()) {
						// Every item from the current stack that is not on the next stack is completed : capture it.
						assert !currentContextStack.peekFirst().isIncomplete() : "Context to close " + currentContextStack.peekFirst() + " must be complete";
						if(capture.test(currentContextStack.removeFirst())) {
							// If the capture function has completed, stop parsing.
							return;
						}
					}
					
					// Finally, navigate to the current "append point" : this is the last child (of the last child of the last child, recursively),
					// of the newly added context, ie the place that may be extended by the next lexeme.
					navigateToAppendPoint(currentContextStack, capture);
				}
				
				// No new context : lexeme was insignificant.
			}
		}
		
		// Stream is empty : no more parsing action in the core parsing logic.
		// Note that the context stack is left as-is, so another parser may take over.
		// Use parseAll if you want to the parser to close and capture the context stack at the end.
	}
	
	private void parseAll(Stream<Lexeme> lexemes, Deque<Context> currentContextStack, ContextType maxDepth, Predicate<Context> capture) {
		parse(lexemes, currentContextStack, maxDepth, capture);
		while(!currentContextStack.isEmpty()) {
			if(capture.test(currentContextStack.removeFirst())) {
				return;
			}
		}
	}
	
	/**
		Fill a root context from a document.
		@param lexemes The stream of lexemes produced by the document.
		@param rootContext The root context of the document.
		@param maxDepth The deepest type of context we wish to consider. Children of these contexts won't be returned in the output.
		@return The given rootContext, filled with all contents retrieved from the document.
	*/
	public final Context fill(Stream<Lexeme> lexemes, Context rootContext, ContextType maxDepth) {
		parseAll(lexemes, new LinkedList<>(List.of(rootContext)), maxDepth, c -> false);
		return rootContext;
	}
	
	/**
		Extract a wanted context from a document.
		@param lexemes The stream of lexemes produced by the document.
		@param rootContext The root context of the document.
		@param maxDepth The deepest type of context we wish to consider. Children of these contexts won't be returned in the output.
		@param wantedContext The metadata of the context we wish to extract.
		@return The context matching the wantedContext metadata, containing all its children up to maxDepth.
	*/
	public final Context extract(Stream<Lexeme> lexemes, Context rootContext, ContextType maxDepth, ContextMetadata wantedContext) {
		Context[] outputContext = { null };
		parseAll(lexemes, new LinkedList<>(List.of(rootContext)), maxDepth, c -> {
			if(Objects.equals(c.metadata, wantedContext)) {
				outputContext[0] = c;
				return true;
			}
			return false;
		});
		return outputContext[0];
	}
}