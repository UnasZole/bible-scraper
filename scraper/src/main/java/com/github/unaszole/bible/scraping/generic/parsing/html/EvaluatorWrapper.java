package com.github.unaszole.bible.scraping.generic.parsing.html;

import com.github.unaszole.bible.scraping.generic.data.VarSubstitution;
import com.github.unaszole.bible.scraping.generic.parsing.ContextualData;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.util.function.Function;

public class EvaluatorWrapper {

    private final Evaluator constant;
    private final Function<ContextualData, Evaluator> contextual;

    public EvaluatorWrapper(final String evaluator) {
        assert evaluator != null;
        if(VarSubstitution.hasVars(evaluator)) {
            constant = null;
            contextual = cd -> QueryParser.parse(VarSubstitution.substituteVars(evaluator, cd.args::get));
        }
        else {
            constant = QueryParser.parse(evaluator);
            contextual = null;
        }
    }

    public Evaluator get(ContextualData contextualData) {
        return constant != null ? constant : contextual.apply(contextualData);
    }
}
