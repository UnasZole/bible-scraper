package com.github.unaszole.bible.datamodel;

public interface ContextType {
	IdType idType();
	ValueType valueType();
	ImplicitValue implicitValue();
	ContextChildrenSpec childrenSpec();
}
