package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents an Inner Join Clause in the query pipeline.
 */
public class InnerJoinClause implements PipelineStage {
    private final StreamPipeline pipelineToJoin;
    private final BFunctionPointer lhsKeyFunction;
    private final BFunctionPointer rhsKeyFunction;
    private final Map<String, List<Frame>> rhsFramesMap = new HashMap<>();
    private BError failureAtJoin = null;
    private final Environment env;

    /**
     * Constructor for the InnerJoinClause.
     *
     * @param env The runtime environment.
     * @param pipelineToJoin The pipeline representing the right-hand side of the join.
     * @param lhsKeyFunction The function to extract the join key from the left-hand side.
     * @param rhsKeyFunction The function to extract the join key from the right-hand side.
     */
    public InnerJoinClause(Environment env, Object pipelineToJoin,
                           BFunctionPointer lhsKeyFunction, BFunctionPointer rhsKeyFunction) {
        this.pipelineToJoin = (StreamPipeline) pipelineToJoin;
        this.lhsKeyFunction = lhsKeyFunction;
        this.rhsKeyFunction = rhsKeyFunction;
        this.env = env;
        initializeRhsFrames();
    }

    /**
     * Initializes the inner join clause.
     *
     * @param env The runtime environment.
     * @param pipelineToJoin The pipeline representing the right-hand side of the join.
     * @param lhsKeyFunction The function to extract the join key from the left-hand side.
     * @param rhsKeyFunction The function to extract the join key from the right-hand side.
     * @return The initialized InnerJoinClause.
     */
    public static InnerJoinClause initInnerJoinClause(Environment env, Object pipelineToJoin,
                                                        BFunctionPointer lhsKeyFunction, BFunctionPointer rhsKeyFunction) {
        return new InnerJoinClause(env, pipelineToJoin, lhsKeyFunction, rhsKeyFunction);
    }

    /**
     * Initializes the right-hand side (RHS) frames by processing the pipeline.
     */
    private void initializeRhsFrames() {
        try {
            Stream<Frame> strm = ((StreamPipeline)StreamPipeline.getStreamFromPipeline(pipelineToJoin)).getStream();
            strm.forEach(frame -> {
                Object key = rhsKeyFunction.call(env.getRuntime(), frame.getRecord());
                if(key instanceof BError) {
                    failureAtJoin = (BError) key;
                    return;
                }
                rhsFramesMap.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(frame);
            });
        } catch (BError e) {
            failureAtJoin = e;
        }
    }

    /**
     * Executes the inner join by processing the left-hand side (LHS) frames.
     *
     * @param inputStream The input stream of frames (LHS).
     * @return A joined stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) throws BError {
        return inputStream.flatMap(lhsFrame -> {
            try {
                if (failureAtJoin != null) {
                    throw failureAtJoin;
                }
                Object lhsKey = lhsKeyFunction.call(env.getRuntime(), lhsFrame.getRecord());
                if(lhsKey instanceof BError) {
                    throw (BError) lhsKey;
                }
                List<Frame> rhsCandidates = rhsFramesMap.getOrDefault(lhsKey.toString(), Collections.emptyList());

                return rhsCandidates.stream()
                        .map(rhsFrame -> mergeFrames(lhsFrame, rhsFrame));

            } catch (BError e) {
                throw e;
            }
        });
    }

    /**
     * Merges two frames into a single frame.
     *
     * @param lhs The left-hand frame.
     * @param rhs The right-hand frame.
     * @return A merged frame.
     */
    private Frame mergeFrames(Frame lhs, Frame rhs) {
        Frame result = new Frame();

        lhs.getRecord().entrySet().forEach(entry ->
                result.getRecord().put(entry.getKey(), entry.getValue())
        );

        rhs.getRecord().entrySet().forEach(entry ->
                result.getRecord().put(entry.getKey(), entry.getValue())
        );

        return result;
    }
}
