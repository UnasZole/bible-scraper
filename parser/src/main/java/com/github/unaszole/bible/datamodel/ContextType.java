package com.github.unaszole.bible.datamodel;

public interface ContextType {
    String name();
	IdType idType();
	ValueType<?> valueType();
	ImplicitValue implicitValue();
	ContextChildrenSpec childrenSpec();
}
