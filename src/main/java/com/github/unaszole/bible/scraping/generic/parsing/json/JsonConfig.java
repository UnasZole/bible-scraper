package com.github.unaszole.bible.scraping.generic.parsing.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

public class JsonConfig {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static final Configuration JSON_PATH_CONFIG = Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
}
