package com.github.unaszole.bible.osisbuilder.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class Parser<Lexeme> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
	
	protected abstract ContextMetadata readContext(ContextMetadata parent, ContextType type, Lexeme lexeme);
	protected abstract String readContent(ContextMetadata context, Lexeme lexeme);
	
	private ContextMetadata getNextContextMeta(Context parent, Lexeme lexeme, ContextType maxDepth) {
		if(parent.metadata.type == maxDepth) {
			return null;
		}
		
		ContextType parentLastChildType = parent.getLastChildType();
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
	
	public Context extract(Stream<Lexeme> lexemes, ContextMetadata rootContext, ContextMetadata wantedContext, ContextType maxDepth) {
		Context currentContext = new Context(null, rootContext, null);
		
		Iterator<Lexeme> lexIt = lexemes.iterator();
		while(lexIt.hasNext()) {
			Lexeme lexeme = lexIt.next();
			
			// Check if this lexeme opens another context, either as child of the current, or of any of its ancestors.
			Context nextCtxParent = currentContext;
			ContextMetadata nextCtxMeta = getNextContextMeta(currentContext, lexeme, maxDepth);
			Context outputContext = null;
			while(nextCtxParent != null && nextCtxMeta == null) {
				if(Objects.equals(nextCtxParent.metadata, wantedContext)) {
					// Exiting the wanted context : mark it as output.
					outputContext = nextCtxParent;
				}
				nextCtxParent = nextCtxParent.parent;
				if(nextCtxParent != null) {
					nextCtxMeta = getNextContextMeta(nextCtxParent, lexeme, maxDepth);
				}
			}
			
			if(nextCtxMeta != null) {
				// We found a new context.
				if(outputContext != null) {
					// By exiting the wanted context : stop and return it.
					return outputContext;
				}
				
				// Instantiate a new context using the lexeme.
				currentContext = new Context(nextCtxParent, nextCtxMeta, readContent(nextCtxMeta, lexeme));
				nextCtxParent.addChild(currentContext);
			}
		}
		
		// Stream is empty : now go back up our context hierarchy until we find the wanted one !
		while(currentContext != null && !Objects.equals(currentContext.metadata, wantedContext)) {
			currentContext = currentContext.parent;
		}
		return currentContext;
	}
}