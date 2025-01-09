package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.FrameContext;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The WhereClause filters the data in the stream based on the provided condition.
 *
 * @param <T> the type of data stored in the FrameContext
 */
public class WhereClause<T> implements PipelineStage<T> {
    private final Predicate<FrameContext<T>> condition;

    /**
     * Constructs a WhereClause with the specified condition.
     *
     * @param condition the predicate condition used to filter the stream
     */
    public WhereClause(Predicate<FrameContext<T>> condition) {
        this.condition = condition;
    }

    @Override
    public Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input) {
        return input.filter(condition); // Filters based on the predicate condition
    }
}
