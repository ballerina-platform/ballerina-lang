package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.values.BCollection;
import io.ballerina.runtime.internal.query.pipeline.FrameContext;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;

import java.util.Map;
import java.util.stream.Stream;

public class FromClause<T> implements PipelineStage<T> {
    private final Object collection;
    private final Map<String, String> variableMappings;

    public FromClause(Object collection, Map<String, String> variableMappings) {
        this.collection = collection;
        this.variableMappings = variableMappings;
    }

    @Override
    public Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input) {
        Stream<T> dataStream = BallerinaIteratorUtils.toStream((BCollection) collection);

        return dataStream.map(data -> {
            FrameContext<T> frameContext = new FrameContext<>();

            for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                String variableName = entry.getKey();
                String variableExpression = entry.getValue();
                Object variableValue = extractValueFromData(variableExpression, data);
                frameContext.addVariable(variableName, variableValue);
            }

            return frameContext;
        });
    }

    private Object extractValueFromData(String variableExpression, T data) {
        // Logic to extract value from the data object based on the expression
        // This can be dynamic and involve reflection or other mechanisms based on the expression format.
        // For simplicity, let's assume a direct extraction for now.
        if (data instanceof Map) {
            return ((Map<?, ?>) data).get(variableExpression); // For map-based data
        }
        return data;  // Placeholder logic, implement actual extraction based on the expression
    }
}
