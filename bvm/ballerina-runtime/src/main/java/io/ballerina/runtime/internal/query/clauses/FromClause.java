package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

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
    public Stream<Frame> process(Stream<Frame> inputStream) throws BError {
        return inputStream.map(frame -> {
            Object result = transformer.call(env.getRuntime(), frame.getRecord());

            if (result instanceof BMap) {
                frame.updateRecord((BMap<BString, Object>) result);
                return frame;
            } else {
                throw new QueryException((BError) result);
            }
        });
    }
}
