package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `do` clause in the query pipeline that applies a function to each element.
 */
public class DoClause implements PipelineStage {
    private final Environment env;
    private final BFunctionPointer function;

    /**
     * Constructor for the DoClause.
     *
     * @param env The runtime environment.
     * @param function The function to be executed for each frame.
     */
    public DoClause(Environment env, BFunctionPointer function) {
        this.env = env;
        this.function = function;
    }

    public static DoClause initDoClause(Environment env, BFunctionPointer function) {
        return new DoClause(env, function);
    }

    /**
     * Processes each element in the stream by applying the function.
     *
     * @param inputStream The input stream of frames.
     * @return The same stream after applying the function.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) throws BError {
        return inputStream.peek(frame -> {
            try {
                function.call(env.getRuntime(), frame.getRecord());
            } catch (BError e) {
                throw e;
            }
        });
    }
}
