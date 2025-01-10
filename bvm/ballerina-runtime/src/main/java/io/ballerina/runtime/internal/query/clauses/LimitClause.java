package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

public class LimitClause implements PipelineStage {
    private final long limit;

    public LimitClause(long limit) {
        this.limit = limit;
    }

    @Override
    public Stream<Frame> process(Stream<Frame> input) {
        return input.limit(limit);
    }
}
