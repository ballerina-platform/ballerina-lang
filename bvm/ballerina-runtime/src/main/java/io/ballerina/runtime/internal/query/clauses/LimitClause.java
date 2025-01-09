package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.FrameContext;

import java.util.stream.Stream;

public class LimitClause<T> implements PipelineStage<T> {
    private final long limit;

    public LimitClause(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Panic");
        }
        this.limit = limit;
    }

    @Override
    public Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input) {
        return input.limit(limit);
    }
}
