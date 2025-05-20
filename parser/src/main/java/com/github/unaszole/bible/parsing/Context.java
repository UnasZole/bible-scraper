package com.github.unaszole.bible.parsing;

import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Context {

	private static long instanceCounter = 0;

	public final ContextMetadata metadata;
	public final Object value;
	private final List<Context> children;
	public final long contextUniqueId;

	private Context(ContextMetadata metadata, Object value, List<Context> children, long contextUniqueId) {
		this.metadata = metadata;
		try {
			this.value = metadata.type.valueType().of(value);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid value " + value + " for context of type " + metadata.type + " : " + e.getMessage());
		}
		this.children = children;
		this.contextUniqueId = contextUniqueId;
	}

	public Context(ContextMetadata metadata, Object value) {
		this(metadata, value, List.of(), instanceCounter++);
	}

	public Context(ContextMetadata metadata) {
		this(metadata, null);
	}
	
	Context addChild(Context child) {
		List<Context> newChildren = new ArrayList<>(children);
		newChildren.add(child);
		return new Context(metadata, value, newChildren, contextUniqueId);
	}
	
	public Optional<Context> getLastChild() {
		return children.isEmpty() ? Optional.empty() : Optional.of(children.get(children.size() - 1));
	}
	
	public List<ContextType> getAllowedTypesForNextChild() {
		return metadata.type.childrenSpec().getAllowedTypesForNextChild(
			children.stream().map(c -> c.metadata.type).collect(Collectors.toList())
		);
	}

	public ContextMetadata getLastChildOfTypeMeta(final ContextType type) {
		return children.stream()
				.map(c -> c.metadata)
				.filter(m -> m.type == type)
				.reduce((first, second) -> second)
				.orElse(null);
	}
	
	public boolean isIncomplete() {
		return metadata.type.childrenSpec().isIncomplete(
			children.stream().map(c -> c.metadata.type).collect(Collectors.toList())
		);
	}
	
	public Optional<List<Context>> getPathFromAncestor(Context ancestor) {
		if(this == ancestor) {
			return Optional.of(List.of());
		}
		
		for(Context closerAncestor: ancestor.children) {
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
			(value != null ? "{" + value + "}" : "") +
			(!children.isEmpty() ? "(" + children + ")" : "");
	}
}