package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `from` clause in the query pipeline that processes a stream of frames.
 */
public class FromClause implements PipelineStage {

    private final BFunctionPointer transformer;
    private final Environment env;

    /**
     * Constructor for the FromClause.
     *
     * @param transformer The function to transform each frame.
     */
    public FromClause(Environment env, BFunctionPointer transformer) {
        this.transformer = transformer;
        this.env = env;
    }

    public static FromClause initFromClause(Environment env, BFunctionPointer transformer) {
        return new FromClause(env, transformer);
    }

    /**
     * Processes a stream of frames by applying the transformation function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A transformed stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.map(frame -> {
            try {
                Object result = transformer.call(env.getRuntime(), frame.getRecord());
                if (result instanceof Frame) {
                    return (Frame) result;
                } else if (result instanceof BMap) {
                    frame.updateRecord((BMap<BString, Object>) result);
                    return frame;
                } else {
                    throw new RuntimeException("Invalid transformation result: " + result);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error during frame transformation", e);
            }
        });
    }
}
