package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;

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
     * @param env         The runtime environment.
     * @param selector The function to select from each frame.
     */
    public SelectClause(Environment env, BFunctionPointer selector) {
        this.selector = selector;
        this.env = env;
    }

    /**
     * Static initializer for SelectClause.
     *
     * @param env         The runtime environment.
     * @param selector The selector function.
     * @return A new instance of SelectClause.
     */
    public static SelectClause initSelectClause(Environment env, BFunctionPointer selector) {
        return new SelectClause(env, selector);
    }

    /**
     * Processes a stream of frames by applying the transformation function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A transformed stream of frames.
     * @throws Exception If an error occurs during transformation.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
//        Stream<Frame> peek = inputStream.peek(System.out::println);
//        Object[] objects = peek.toArray();
        try {
            return inputStream.map(
                    frame -> (Frame) selector.call(env.getRuntime(), frame)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
