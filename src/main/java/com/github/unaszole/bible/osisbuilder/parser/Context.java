package com.github.unaszole.bible.osisbuilder.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	public void addChild(Context child) {
		children.add(child);
	}
	
	public ContextType getLastChildType() {
		return children.isEmpty() ? null : children.get(children.size() - 1).metadata.type;
	}
	
	public List<Context> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	@Override
	public String toString() {
		return metadata +
			(content != null ? "{" + content + "}" : "") +
			(!children.isEmpty() ? "(" + children + ")" : "");
	}
}