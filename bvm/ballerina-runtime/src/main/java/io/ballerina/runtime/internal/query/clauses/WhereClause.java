package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `where` clause in the query pipeline that filters a stream of frames.
 */
public class WhereClause implements PipelineStage {
    private final BFunctionPointer filterFunc;
    private final Environment env;

    /**
     * Constructor for the WhereClause.
     *
     * @param filterFunc The function to filter frames.
     */
    public WhereClause(Environment env, BFunctionPointer filterFunc) {
        this.filterFunc = filterFunc;
        this.env = env;
    }

    public static WhereClause initWhereClause(Environment env, BFunctionPointer filterFunc) {
        return new WhereClause(env, filterFunc);
    }

    /**
     * Filters a stream of frames by applying the filter function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A filtered stream of frames.
     * @throws Exception If an error occurs during filtering.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) throws Exception {
        return inputStream.filter(frame -> {
            try {
                // Apply the filter function to the current frame and evaluate the result.
                Object result = filterFunc.call(env.getRuntime(), frame.getRecord().get("value"));
                if (result instanceof Boolean) {
                    return (Boolean) result; // Include frame if predicate is true.
                }
                throw new IllegalStateException("Filter function must return a boolean.");
            } catch (Exception e) {
                throw new RuntimeException("Error applying filter function.", e);
            }
        });
    }
}
