package com.github.unaszole.bible.datamodel;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ContextType {
    String name();
	IdType idType();
	ValueType<?> valueType();
	ImplicitValue implicitValue();
	ContextChildrenSpec childrenSpec();

	default Map<String, ContextType> descendantTypesByName() {
		return Stream.concat(
				Stream.of(this),
				this.childrenSpec().getDescendants().stream()
		).collect(Collectors.toMap(ContextType::name, ct -> ct));
	}
}
