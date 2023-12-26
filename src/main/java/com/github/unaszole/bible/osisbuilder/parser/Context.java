package com.github.unaszole.bible.osisbuilder.parser;

import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Context {
	
	public static Context docRoot(BibleBook book) {
		return new Context(
			null,
			new ContextMetadata(ContextType.DOCUMENT, book, 0, 0),
			null
		);
	}
	
	public final Context parent;
	public final ContextMetadata metadata;
	public final String content;
	private List<Context> children = new ArrayList<>();
	
	public Context(Context parent, ContextMetadata metadata, String content) {
		this.parent = parent;
		this.metadata = metadata;
		this.content = content;
	}
	
	public void addChild(Context child) {
		children.add(child);
	}
	
	public ContextType getLastChildType() {
		return children.isEmpty() ? null : children.get(children.size() - 1).metadata.type;
	}
	
	public List<Context> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public boolean isDescendantOf(ContextMetadata otherContext) {
		Context currentAncestor = this;
		while(currentAncestor != null) {
			if(Objects.equals(currentAncestor.metadata, otherContext)) {
				return true;
			}
			currentAncestor = currentAncestor.parent;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return metadata +
			(content != null ? "{" + content + "}" : "") +
			(!children.isEmpty() ? "(" + children + ")" : "");
	}
}