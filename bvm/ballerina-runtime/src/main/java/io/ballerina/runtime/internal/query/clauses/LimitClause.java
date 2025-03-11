package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `limit` clause in the query pipeline that restricts the number of frames.
 */
public class LimitClause implements PipelineStage {
    private final BFunctionPointer limitFunction;
    private final Environment env;

    /**
     * Constructor for the LimitClause.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     */
    public LimitClause(Environment env, BFunctionPointer limitFunction) {
        this.limitFunction = limitFunction;
        this.env = env;
    }

    /**
     * Static initializer for LimitClause.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     * @return A new instance of LimitClause.
     */
    public static LimitClause initLimitClause(Environment env, BFunctionPointer limitFunction) {
        return new LimitClause(env, limitFunction);
    }

    /**
     * Processes a stream of frames by applying the limit function to determine the maximum number of frames.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of frames with at most `limit` frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        // Evaluate the limit function only once.
        Object limitResult = limitFunction.call(env.getRuntime(), new Frame().getRecord());
        if (limitResult instanceof BError) {
            throw (BError) limitResult;
        }
        Long limit = (Long) limitResult;
        return inputStream.limit(limit);
    }
}
