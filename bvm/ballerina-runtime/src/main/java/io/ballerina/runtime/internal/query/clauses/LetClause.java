package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `let` clause in the query pipeline that modifies frames.
 */
public class LetClause implements PipelineStage {
    private final BFunctionPointer frameModifier;
    private final Environment env;

    /**
     * Constructor for the LetClause.
     *
     * @param env           The runtime environment.
     * @param frameModifier The function to modify the frame.
     */
    public LetClause(Environment env, BFunctionPointer frameModifier) {
        this.frameModifier = frameModifier;
        this.env = env;
    }

    /**
     * Static initializer for LetClause.
     *
     * @param env           The runtime environment.
     * @param frameModifier The function to modify the frame.
     * @return A new instance of LetClause.
     */
    public static LetClause initLetClause(Environment env, BFunctionPointer frameModifier) {
        return new LetClause(env, frameModifier);
    }

    /**
     * Processes a stream of frames by applying the modifier function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of modified frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.map(frame -> {
            Object result = frameModifier.call(env.getRuntime(), frame.getRecord());
            if (result instanceof Frame) {
                return (Frame) result;
            }  else if (result instanceof BMap) {
                frame.updateRecord((BMap<BString, Object>) result);
                return frame;
            } else {
                throw (BError) result;
            }
        });
    }
}
