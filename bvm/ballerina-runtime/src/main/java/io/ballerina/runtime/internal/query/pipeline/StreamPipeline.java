package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.query.clauses.PipelineStage;
import io.ballerina.runtime.internal.query.utils.BallerinaIteratorUtils;
import io.ballerina.runtime.internal.query.utils.DistinctQueryErrorCreator;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A class that represents a stream pipeline for processing data.
 */
public class StreamPipeline {

    private Stream<Frame> stream;
    private Supplier<Stream<Frame>> streamSupplier;
    private final List<PipelineStage> pipelineStages;
    private final BTypedesc constraintType;
    private final BTypedesc completionType;
    private final boolean isLazyLoading;
    private final Environment env;
    private final Iterator<?> itr;

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
//        this.stream = initializeFrameStream(env, collection);
        this.pipelineStages = new ArrayList<>();
        this.constraintType = constraintType;
        this.completionType = completionType;
        this.isLazyLoading = isLazyLoading;
        this.itr = BallerinaIteratorUtils.getIterator(env, collection);
//        if(isLazyLoading) {
//            this.stream = initializeFrameStream(env, itr);
//            this.streamSupplier = () -> initializeFrameStream(env, itr);
//        } else {
            this.stream = initializeFrameStream(env, itr);
//        }
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

    public static Object getStreamFromPipeline(Object pipeline){
        try {
            ((StreamPipeline)pipeline).execute();
        } catch (BError e) {
            return e;
        }

//        return CollectionUtil.toBStream((StreamPipeline) pipeline);
        return (StreamPipeline) pipeline;
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
    public void execute() throws BError {
        for (PipelineStage stage : pipelineStages) {
            stream = stage.process(stream);
        }
    }

    /**
     * Initializes a stream of `Frame` objects from the provided Ballerina collection.
     *
     * @param itr The iterator for the Ballerina collection.
     * @return A Java stream of `Frame` objects.
     */
    private Stream<Frame> initializeFrameStream(Environment env, Iterator<?> itr) throws ErrorValue {
        return BallerinaIteratorUtils.toStream(env, itr);
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
//        if (isLazyLoading) {
//            try {
//                stream.iterator();
//            } catch (IllegalStateException e) {
//                stream = streamSupplier.get();
//                try {
//                    execute();
//                } catch (Exception ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        }
        return stream;
    }

//    public Stream<Frame> getStreamForJoin(){
//        return stream;
//    }
//
//    public static boolean isConsumed(Stream<Frame> stream) {
//        try {
//            stream.iterator();
//            return false;
//        } catch (IllegalStateException e) {
//            return true;
//        }
//    }

}
