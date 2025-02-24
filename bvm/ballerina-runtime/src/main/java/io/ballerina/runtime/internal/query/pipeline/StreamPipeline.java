package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.query.clauses.PipelineStage;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;
import io.ballerina.runtime.internal.query.utils.CollectionUtil;
import io.ballerina.runtime.internal.values.ErrorValue;

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
    private final boolean isLazyLoading;
    private final Environment env;

    /**
     * Constructor for creating a StreamPipeline.
     *
     * @param collection      The collection to process.
     * @param constraintType  The type descriptor for the constraint type.
     * @param completionType  The type descriptor for the completion type.
     * @param isLazyLoading   Flag to indicate if lazy loading is enabled.
     */
    public StreamPipeline(Environment env,
                          Object collection,
                          BTypedesc constraintType,
                          BTypedesc completionType,
                          boolean isLazyLoading) {
        this.env = env;
        this.stream = initializeFrameStream(env, collection);
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
        this.isLazyLoading = isLazyLoading;
    }

    public static Object initStreamPipeline(Environment env, Object collection,
                                                    BTypedesc constraintType,
                                                    BTypedesc completionType,
                                                    boolean isLazyLoading) {
        try {
            return new StreamPipeline(env, collection, constraintType, completionType, isLazyLoading);
        } catch (ErrorValue e) {
            return e;
        }
    }

    public static void addStreamFunction(Object jStreamPipeline, Object pipelineStage) {
        ((StreamPipeline) jStreamPipeline).addStage((PipelineStage) pipelineStage);
    }

    public static StreamPipeline getStreamFromPipeline(Object pipeline){
        try {
            ((StreamPipeline)pipeline).execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        return CollectionUtil.toBStream((StreamPipeline) pipeline);
        return ((StreamPipeline) pipeline);
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
    private Stream<Frame> initializeFrameStream(Environment env, Object collection) throws ErrorValue {
        return BallerinaIteratorUtils.toStream(env, collection);
    }

    /**
     * Resets the pipeline for reuse.
     */
    public void reset() {

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
