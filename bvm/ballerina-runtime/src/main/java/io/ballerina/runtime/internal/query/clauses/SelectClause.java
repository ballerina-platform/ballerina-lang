package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.query.pipeline.ErrorFrame;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.stream.Stream;

/**
 * Represents a `select` clause in the query pipeline that processes a stream of frames.
 */
public class SelectClause implements PipelineStage {
    private final BFunctionPointer selector;
    private final Environment env;

    /**
     * Constructor for the SelectClause.
     *
     * @param env      The runtime environment.
     * @param selector The function to select from each frame.
     */
    public SelectClause(Environment env, BFunctionPointer selector) {
        this.selector = selector;
        this.env = env;
    }

    /**
     * Static initializer for SelectClause.
     *
     * @param env      The runtime environment.
     * @param selector The selector function.
     * @return A new instance of SelectClause.
     */
    public static SelectClause initSelectClause(Environment env, BFunctionPointer selector) {
        return new SelectClause(env, selector);
    }

    /**
     * Processes a stream of frames by applying the selector function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A transformed stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) throws BError {
        return inputStream.map(frame -> {
            Object result = selector.call(env.getRuntime(), frame.getRecord());
            if (result instanceof BMap mapVal) {
                frame.updateRecord(mapVal);
                return frame;
            } else if (result instanceof BError error) {
                throw new QueryException(error);
            }
            return frame;
        });
    }
}
