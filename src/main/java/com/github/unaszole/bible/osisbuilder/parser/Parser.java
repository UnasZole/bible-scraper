package com.github.unaszole.bible.osisbuilder.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
	A parser processes a stream of lexemes (ie atomic significative elements) from a source document.
	If the source document is an HTML page, the lexemes will typically be the HTML elements in document order.
	
	@param <Lexeme> The type of lexeme processed by this parser.
*/
public abstract class Parser<Lexeme> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
	
	/**
		@param parent The metadata of the parent context, to use if this lexeme does open a new context.
		@param type The type of context to open from this lexeme.
		@param lexeme The lexeme to analyse.
		@return Metadata for a new context extracted from the lexeme, or null if this lexeme does not open a context of the requested type.
	*/
	protected abstract ContextMetadata readContext(ContextMetadata parent, ContextType type, Lexeme lexeme);
	
	/**
		@param context The metadata of context to be opened by the given lexeme.
		@param lexeme The lexeme to analyse.
		@return The content extracted from that lexeme to feed directly into the new context, or null if this lexeme does not contain relevant contents for this context.
	*/
	protected abstract String readContent(ContextMetadata context, Lexeme lexeme);
	
	/**
		@param lexeme The lexeme to parse with an external logic.
		@param currentContext The current context when this lexeme was reached.
		@param maxDepth The deepest type of context we wish to parse. Descendants of these contexts will be ignored.
		@param capture A function that consumes all completed contexts in order. Returns true if the parsing should be stopped.
		@return True if the lexeme was handled by external parsing (and thus should be ignored by this parser), false otherwise.
	*/
	protected boolean parseExternally(Lexeme lexeme, Context currentContext, ContextType maxDepth, Predicate<Context> capture) {
		// No manual parsing is done by default.
		// This method should be overridden by implementations if some lexemes require manual parsing.
		return false;
	}
	
	private ContextMetadata getNextContextMeta(Context parent, Lexeme lexeme, ContextType maxDepth) {
		if(parent.metadata.type == maxDepth) {
			return null;
		}
		
		ContextType parentLastChildType = parent.getLastChild().map(c -> c.metadata.type).orElse(null);
		boolean allowedFromNow = parentLastChildType == null;
		for(ContextType childType: parent.metadata.type.allowedChildren) {
			if(childType == parentLastChildType) {
				// The child type we're examining is allowed after the current last child.
				allowedFromNow = true;
			}
			
			if(allowedFromNow) {
				ContextMetadata nextCtxMeta = readContext(parent.metadata, childType, lexeme);
				if(nextCtxMeta != null) {
					return nextCtxMeta;
				}
			}
		}
		return null;
	}
	
	/**
		@param lexemes The stream of lexemes to parse.
		@param rootContext The context we are in when starting the parse, that will be filled while parsing.
		@param maxDepth The deepest type of context we wish to parse. Descendants of these contexts will be ignored.
		@param capture A function that consumes all completed contexts in order. Returns true if the parsing should be stopped.
	*/
	public final void parse(Stream<Lexeme> lexemes, Context rootContext, ContextType maxDepth, Predicate<Context> capture) {
		Context currentContext = rootContext;
		
		Iterator<Lexeme> lexIt = lexemes.iterator();
		while(lexIt.hasNext()) {
			Lexeme lexeme = lexIt.next();
			
			if(!parseExternally(lexeme, currentContext, maxDepth, capture))
			{
				// Check if this lexeme opens another context, either as child of the current, or of any of its ancestors.
				Context nextCtxParent = currentContext;
				ContextMetadata nextCtxMeta = null;
				while(nextCtxParent != null && nextCtxMeta == null) {
					nextCtxMeta = getNextContextMeta(nextCtxParent, lexeme, maxDepth);
					
					if(nextCtxMeta == null) {
						nextCtxParent = nextCtxParent.parent;
					}
				}
				
				if(nextCtxMeta != null) {
					// Lexeme starts a new context.
					
					if(Objects.equals(nextCtxMeta, nextCtxParent.getLastChild().map(c -> c.metadata).orElse(null))) {
						// New context is identical to its previous sibling : nothing to do, just set it as current.
						currentContext = nextCtxParent.getLastChild().get();
					}
					else {
						// It really starts a new context !
						
						// All ancestors of the current context which are not ancestors of the new context are now complete.
						// Send them to capture.
						Context currentAncestor = currentContext;
						while(currentAncestor != null && !nextCtxParent.isDescendantOf(currentAncestor.metadata)) {
							if(capture.test(currentAncestor)) {
								// If the capture function has completed, stop parsing.
								return;
							}
							
							currentAncestor = currentAncestor.parent;
						}
						
						// Instantiate a new context using the lexeme.
						currentContext = new Context(nextCtxParent, nextCtxMeta, readContent(nextCtxMeta, lexeme));
						nextCtxParent.addChild(currentContext);
					}
				}
				
				// No new context : lexeme was insignificant.
			}
		}
		
		// Stream is empty : time to close and capture the current context and all its ancestors.
		while(currentContext != null) {
			if(capture.test(currentContext)) {
				return;
			}
			currentContext = currentContext.parent;
		}
	}
	
	public final Context parse(Stream<Lexeme> lexemes, Context rootContext, ContextType maxDepth) {
		parse(lexemes, rootContext, maxDepth, c -> false);
		return rootContext;
	}
	
	/**
		@param lexemes The stream of lexemes to parse.
		@param rootContext The context we are in when starting the parse.
		@param maxDepth The deepest type of context we wish to extract. Children of these contexts won't be returned in the output.
		@param wantedContext The metadata of the context we wish to extract.
		@return The context matching the wantedContext metadata, containing all its children up to maxDepth.
	*/
	public final Context extract(Stream<Lexeme> lexemes, Context rootContext, ContextType maxDepth, ContextMetadata wantedContext) {
		Context[] outputContext = { null };
		parse(lexemes, rootContext, maxDepth, c -> {
			if(Objects.equals(c.metadata, wantedContext)) {
				outputContext[0] = c;
				return true;
			}
			return false;
		});
		return outputContext[0];
	}
}