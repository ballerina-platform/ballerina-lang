package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.FrameContext;

import java.util.function.Function;
import java.util.stream.Stream;

public class SelectClause<T> implements PipelineStage<T> {
    private final Function<FrameContext<T>, Object> transform;

    public SelectClause(Function<FrameContext<T>, Object> transform) {
        this.transform = transform;
    }

    @Override
    public Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input) {
        return input.map(frame -> {
            frame.addVariable("result", transform.apply(frame));
            return frame;
        });
    }
}
