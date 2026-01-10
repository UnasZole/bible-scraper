package com.github.unaszole.bible.scraping.generic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.contexttypes.FlatText;
import com.github.unaszole.bible.scraping.generic.data.Bible;
import com.github.unaszole.bible.scraping.generic.data.PatternContainer;
import com.github.unaszole.bible.scraping.generic.parsing.TextParser;
import com.github.unaszole.bible.scraping.generic.parsing.html.EvaluatorWrapper;
import com.jayway.jsonpath.JsonPath;
import org.crosswire.jsword.versification.BibleBook;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {

    public enum ParserType {
        BIBLE(BibleContainers.BIBLE);

        private final Map<String, ContextType> allowedContexts;

        ParserType(ContextType rootContextType) {
            this.allowedContexts = rootContextType.childrenSpec().getDescendants().stream()
                    .collect(Collectors.toMap(ContextType::name, ct -> ct));
        }

        public ContextType contextTypeOf(String typeName) {
            return Optional.ofNullable(allowedContexts.get(typeName))
                    .orElseThrow(() -> new RuntimeException("Unknown context type " + typeName + " for parser type " + name()));
        }
    }

    public static class NamedTextParser extends TextParser {
        public String id;
        public ParserType type;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()).registerModule(new SimpleModule()
            .addDeserializer(EvaluatorWrapper.class, new StdDeserializer<>(EvaluatorWrapper.class) {
                @Override
                public EvaluatorWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return new EvaluatorWrapper(jsonParser.readValueAs(String.class));
                }
            })
            .addDeserializer(JsonPath.class, new StdDeserializer<>(JsonPath.class) {
                @Override
                public JsonPath deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return JsonPath.compile(jsonParser.readValueAs(String.class));
                }
            })
            .addDeserializer(BibleBook.class, new StdDeserializer<>(BibleBook.class) {
                @Override
                public BibleBook deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return BibleBook.fromOSIS(jsonParser.readValueAs(String.class));
                }
            })
            .addDeserializer(Pattern.class, new StdDeserializer<>(Pattern.class) {
                @Override
                public Pattern deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    return Pattern.compile(jsonParser.readValueAs(String.class), Pattern.DOTALL);
                }
            })
            .addDeserializer(ContextType.class, new StdDeserializer<>(ContextType.class) {
                @Override
                public ContextType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                        throws IOException {
                    // Look up the ancestor stack for the root named text parser.
                    JsonStreamContext ancestorCtx = jsonParser.getParsingContext();
                    while(ancestorCtx != null && !(ancestorCtx.getCurrentValue() instanceof NamedTextParser)) {
                        ancestorCtx = ancestorCtx.getParent();
                    }

                    // Determine the parser type, defaulting to BIBLE.
                    ParserType parserType = ParserType.BIBLE;
                    if(ancestorCtx != null && ancestorCtx.getCurrentValue() instanceof NamedTextParser) {
                        NamedTextParser parser = (NamedTextParser) ancestorCtx.getCurrentValue();
                        if(parser.type != null) {
                            parserType = parser.type;
                        }
                    }

                    return parserType.contextTypeOf(jsonParser.readValueAs(String.class));
                }
            })
    );

    public static Config parse(InputStream is) throws IOException {
        return MAPPER.readValue(is, Config.class);
    }

    public String description;
    public List<String> inputs;
    public Bible bible;
    public List<NamedTextParser> parsers;

    public PatternContainer getGlobalDefaults(List<String> inputValues) {
        int nbExpectedFlags = inputs == null ? 0 : inputs.size();
        int nbGivenFlags = inputValues.size();
        if (nbGivenFlags != nbExpectedFlags) {
            throw new IllegalArgumentException("Provided " + nbGivenFlags + " flags (" + inputValues + "), while expecting "
                    + nbExpectedFlags + (inputs == null ? "" : " (" + inputs + ")")
            );
        }

        PatternContainer globalDefaults = new PatternContainer();
        globalDefaults.args = new HashMap<>();
        for (int i = 0; i < inputValues.size(); i++) {
            globalDefaults.args.put(inputs.get(i), inputValues.get(i));
        }
        return globalDefaults;
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
