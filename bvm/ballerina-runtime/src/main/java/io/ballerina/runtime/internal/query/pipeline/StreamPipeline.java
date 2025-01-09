package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BCollection;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * A class that represents a stream pipeline for processing data.
 */
public class StreamPipeline<T, R> {

    private final Stream<T> stream;
    private final List<Function<T, R>> pipelineStages;
    private final BTypedesc constraintType;
    private final BTypedesc completionType;

    /**
     * Constructor for creating a StreamPipeline.
     *
     * @param collection      The collection to process (Ballerina `BCollection`).
     * @param constraintType  The type descriptor for the constraint type.
     * @param completionType  The type descriptor for the completion type.
     * @param isLazyLoading   Flag to indicate if lazy loading is enabled.
     */
    public StreamPipeline(Object collection, BTypedesc constraintType, BTypedesc completionType, boolean isLazyLoading) {
        int a = 8;
        this.stream = initializeStream((BCollection) collection);
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
    }

    /**
     * Adds a stage (function) to the pipeline.
     *
     * @param stage A function to process each element.
     */
    public void addStage(Function<T, R> stage) {
        this.pipelineStages.add(stage);
    }

    /**
     * Executes the pipeline and returns the result stream.
     *
     * @return A stream of results after applying all pipeline stages.
     */
    public Stream<R> executePipeline() {
        Stream<R> resultStream = this.stream.map(element -> {
            R result = null;
            for (Function<T, R> stage : this.pipelineStages) {
                result = stage.apply(element);
            }
            return result;
        });
        return resultStream;
    }

    /**
     * Converts the pipeline's result stream to a list of frames.
     *
     * @return A list of Ballerina `BMap` (frames).
     */
    public List<R> toFrames() {
        Stream<R> resultStream = executePipeline();
        List<R> frames = new ArrayList<>();
        resultStream.forEach(frames::add);
        return frames;
    }

    /**
     * Initializes a Java stream from the provided Ballerina collection using BallerinaIteratorUtils.
     *
     * @param collection The Ballerina collection.
     * @return A Java stream of elements.
     */
    private Stream<T> initializeStream(BCollection collection) {
        return BallerinaIteratorUtils.toStream(collection);
    }

    /**
     * Resets the pipeline for reuse.
     */
    public void reset() {
        // Logic to reset the pipeline, if applicable
    }

    public BTypedesc getConstraintType() {
        return constraintType;
    }

    public BTypedesc getCompletionType() {
        return completionType;
    }
}
