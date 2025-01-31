package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.utils.StringUtils;
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
     * @param collection      The collection to process.
     * @param constraintType  The type descriptor for the constraint type.
     * @param completionType  The type descriptor for the completion type.
     * @param isLazyLoading   Flag to indicate if lazy loading is enabled.
     */
    public StreamPipeline(Object collection,
                          BTypedesc constraintType,
                          BTypedesc completionType,
                          boolean isLazyLoading) {
        this.stream = initializeFrameStream(collection);
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
    }

    public static StreamPipeline initStreamPipeline(Object collection,
                                          BTypedesc constraintType,
                                          BTypedesc completionType,
                                          boolean isLazyLoading) {
        return new StreamPipeline(collection, constraintType, completionType, isLazyLoading);
    }

    public static void addStreamFunction(Object jStreamPipeline, Object pipelineStage) {
        ((StreamPipeline) jStreamPipeline).addStage((PipelineStage) pipelineStage);
    }

    public static Stream<Frame> getStreamFromPipeline(Object pipeline){
        try {
            ((StreamPipeline)pipeline).execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ((StreamPipeline) pipeline).getStream();
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
     */
    public void execute() throws Exception {
        for (PipelineStage stage : pipelineStages) {
            stream = stage.process(stream);
        }
    }

    /**
     * Initializes a stream of `Frame` objects from the provided Ballerina collection.
     *
     * @param collection The Ballerina collection.
     * @return A Java stream of `Frame` objects.
     */
    private Stream<Frame> initializeFrameStream(Object collection) {
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

    public Stream<Frame> getStream() {
        return stream;
    }


}
