package com.github.unaszole.bible.datamodel;

import java.util.Objects;

public class ContextMetadata {
	public final ContextType type;
	public final ContextId id;
	
	public ContextMetadata(ContextType type, ContextId id) {
		this.type = type;
		this.id = id;
	}

	public ContextMetadata(ContextType type) {
		this(type, null);
	}
	
	@Override
	public String toString() {
		return type + (id != null ? "=" + id : "");
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ContextMetadata that = (ContextMetadata) o;
		return type == that.type && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, id);
	}
}