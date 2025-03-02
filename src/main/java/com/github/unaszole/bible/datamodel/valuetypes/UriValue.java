package com.github.unaszole.bible.datamodel.valuetypes;

import com.github.unaszole.bible.datamodel.ValueType;

import java.net.URI;

public class UriValue implements ValueType<URI> {
    @Override
    public Class<URI> getValueClass() {
        return URI.class;
    }

    @Override
    public URI valueOf(String value) throws IllegalArgumentException {
        return java.net.URI.create(value);
    }
}
