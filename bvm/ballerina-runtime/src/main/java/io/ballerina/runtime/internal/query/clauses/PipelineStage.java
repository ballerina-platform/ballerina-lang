package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Interface representing a stage in the StreamPipeline.
 */
@FunctionalInterface
public interface PipelineStage {

    /**
     * Processes a stream of frames and returns a transformed stream of frames.
     *
     * @param inputStream The input stream of frames.
     * @return The transformed stream of frames.
     */
    Stream<Frame> process(Stream<Frame> inputStream) throws Exception;
}
