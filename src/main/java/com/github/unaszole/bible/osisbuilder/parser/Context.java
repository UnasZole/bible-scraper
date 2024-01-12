package com.github.unaszole.bible.osisbuilder.parser;

import org.crosswire.jsword.versification.BibleBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Context {
	
	public final ContextMetadata metadata;
	public final String content;
	private List<Context> children = new ArrayList<>();
	
	public Context(ContextMetadata metadata, Context... children) {
		this.metadata = metadata;
		this.children.addAll(List.of(children));
		this.content = null;
	}
	
	public Context(ContextMetadata metadata, String content) {
		this.metadata = metadata;
		this.content = content;
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
	
	public Set<ContextType> getAllowedTypesForNextChild() {
		return metadata.type.getAllowedTypesForNextChild(
			children.stream().map(c -> c.metadata.type).collect(Collectors.toList())
		);
	}
	
	public boolean isIncomplete() {
		return metadata.type.isIncomplete(
			children.stream().map(c -> c.metadata.type).collect(Collectors.toList())
		);
	}
	
	public Optional<List<Context>> getPathFromAncestor(Context ancestor) {
		if(this == ancestor) {
			return Optional.of(List.of());
		}
		
		for(Context closerAncestor: ancestor.getChildren()) {
			Optional<List<Context>> pathFromCloserAncestor = this.getPathFromAncestor(closerAncestor);
			
			if(pathFromCloserAncestor.isPresent()) {
				List<Context> output = new ArrayList<>();
				output.add(closerAncestor);
				output.addAll(pathFromCloserAncestor.get());
				return Optional.of(output);
			}
		}
		
		// Given context is actually not an ancestor, no path.
		return Optional.empty();
	}
	
	@Override
	public String toString() {
		return metadata +
			(content != null ? "{" + content + "}" : "") +
			(!children.isEmpty() ? "(" + children + ")" : "");
	}
}