package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BCollection;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.query.clauses.PipelineStage;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A class that represents a stream pipeline for processing data.
 */
public class StreamPipeline {

    private Stream<Frame> stream;
    private final List<PipelineStage> pipelineStages;
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
    public StreamPipeline(Object collection,
                          BTypedesc constraintType,
                          BTypedesc completionType,
                          boolean isLazyLoading) {
        int a = 2;
        this.stream = initializeFrameStream((BCollection) collection);
        String s = stream.toString();
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
    }

    public void addStreamFunction(Object jStreamPipeline, Object pipelineStage) {
        ((StreamPipeline) jStreamPipeline).addStage((PipelineStage) pipelineStage);
    }
    /**
     * Adds a stage (function) to the pipeline.
     *
     * @param stage A function to process each frame.
     */
    public void addStage(PipelineStage stage) {
        this.pipelineStages.add(stage);
    }

    /**
     * Processes the stream through all the pipeline stages.
     *
     * @return A processed stream of `Frame` objects.
     */
    public Stream<Frame> execute() throws Exception {
        Stream<Frame> currentStream = this.stream;
//        for (Function<Frame, Frame> stage : pipelineStages) {
//            currentStream = currentStream.map(stage);
//        }
        for (PipelineStage stage : pipelineStages) {
            stream = stage.process(stream);
        }
        return currentStream;
    }

    /**
     * Initializes a stream of `Frame` objects from the provided Ballerina collection.
     *
     * @param collection The Ballerina collection.
     * @return A Java stream of `Frame` objects.
     */
    private Stream<Frame> initializeFrameStream(BCollection collection) {
        return BallerinaIteratorUtils.toStream(collection)
                .map(element -> Frame.add(StringUtils.fromString("value"), element));
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
