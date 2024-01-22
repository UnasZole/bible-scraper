package com.github.unaszole.bible.scraping;

import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;

import java.util.Objects;

public interface ContextConsumer {
    /**
     * OPEN means the given context was just opened. Only its metadata and direct content is relevant, all its children are missing.
     * CLOSE means the given context is complete : all of its children are included.
     */
    enum EventType {OPEN, CLOSE};

    /**
     * CONTINUE means the parser or scraper should keep going to fetch more data.
     * TERMINATE means the parser or scraper should stop processing, as all needed data have already been fetched.
     */
    enum Instruction {CONTINUE, TERMINATE}

    /**
     * Consume a context event.
     * @param type Opening or closing of the context. See {@link EventType}.
     * @param context The consumed context.
     * @return An instruction for the parser or scraper. See {@link Instruction}
     */
    Instruction consume(EventType type, Context context);

    static ContextConsumer.Instruction consumeAll(ContextConsumer consumer, Context context) {
        ContextConsumer.Instruction out = consumer.consume(ContextConsumer.EventType.OPEN, context);
        if(out == ContextConsumer.Instruction.TERMINATE) {
            return out;
        }

        for(Context child: context.getChildren()) {
            out = consumeAll(consumer, child);
            if(out == ContextConsumer.Instruction.TERMINATE) {
                return out;
            }
        }
        return consumer.consume(ContextConsumer.EventType.CLOSE, context);
    }

    ContextConsumer PARSE_ALL = (t, c) -> Instruction.CONTINUE;

    class Extractor implements ContextConsumer {

        private final ContextMetadata wantedContext;
        private Context output = null;

        public Extractor(ContextMetadata wantedContext) {
            this.wantedContext = wantedContext;
        }

        @Override
        public Instruction consume(EventType type, Context context) {
            if(type == EventType.CLOSE && Objects.equals(context.metadata, wantedContext)) {
                output = context;
                return Instruction.TERMINATE;
            }
            return Instruction.CONTINUE;
        }

        public Context getOutput() {
            return output;
        }
    }
}
