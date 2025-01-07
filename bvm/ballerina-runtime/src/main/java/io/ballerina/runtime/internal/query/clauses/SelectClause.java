package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.FrameContext;

import java.util.Map;
import java.util.stream.Stream;

public class SelectClause<T> implements PipelineStage<T> {
    private final Map<String, String> selectMappings;

    /**
     * Constructs a SelectClause with the mappings for variable selection.
     *
     * @param selectMappings A map where the key is the output variable name,
     *                       and the value is the input variable name in the context.
     */
    public SelectClause(Map<String, String> selectMappings) {
        this.selectMappings = selectMappings;
    }

    @Override
    public Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input) {
        return input.map(context -> {
            FrameContext<T> newContext = new FrameContext<>();

            // Map selected variables to the new context
            for (Map.Entry<String, String> entry : selectMappings.entrySet()) {
                String outputVariable = entry.getKey();
                String inputVariable = entry.getValue();
                Object value = context.getVariable(inputVariable);
                newContext.addVariable(outputVariable, value);
            }

            return newContext;
        });
    }
}
