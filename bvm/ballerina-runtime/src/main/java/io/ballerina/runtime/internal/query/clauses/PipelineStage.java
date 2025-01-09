package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.FrameContext;

import java.util.stream.Stream;

/**
 * Interface representing a stage in the pipeline.
 * Each stage processes a stream of FrameContext objects and produces a transformed stream.
 *
 * @param <T> The type of the data within the FrameContext.
 */
public interface PipelineStage<T> {
    /**
     * Applies the operation defined by this pipeline stage.
     *
     * @param input The input stream of FrameContext objects.
     * @return The transformed stream of FrameContext objects.
     */
    Stream<FrameContext<T>> apply(Stream<FrameContext<T>> input);
}
