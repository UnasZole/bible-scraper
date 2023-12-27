package com.github.unaszole.bible.osisbuilder.parser;

import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Context {
	
	public final Context parent;
	public final ContextMetadata metadata;
	public final String content;
	private List<Context> children = new ArrayList<>();
	
	public Context(Context parent, ContextMetadata metadata, String content) {
		this.parent = parent;
		this.metadata = metadata;
		this.content = content;
	}
	
	public Context(ContextMetadata metadata) {
		this(null, metadata, null);
	}
	
	public void addChild(Context child) {
		children.add(child);
	}
	
	public Optional<Context> getLastChild() {
		return children.isEmpty() ? Optional.empty() : Optional.of(children.get(children.size() - 1));
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