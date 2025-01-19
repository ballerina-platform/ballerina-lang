package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

/**
 * Represents a `from` clause in the query pipeline that processes a stream of frames.
 */
public class FromClause implements PipelineStage {
//    private final Function<Frame, Frame> transformer;
    private final BFunctionPointer transformer;
    private final Environment env;

    /**
     * Constructor for the FromClause.
     *
     * @param transformer The function to transform each frame.
     */
    public FromClause(Environment env , BFunctionPointer transformer) {
        int a = 2;
        int c = 3;
        this.transformer = transformer;
        this.env = env;
    }

    public static FromClause initFromClause(Environment env , BFunctionPointer transformer) {
        return new FromClause(env , transformer);
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
                    frame -> (Frame) transformer.call(env.getRuntime(), frame.getRecord())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
