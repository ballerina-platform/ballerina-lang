package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import io.ballerina.runtime.internal.query.utils.QueryErrorValue;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents an Outer Join Clause in the query pipeline.
 */
public class OuterJoinClause implements PipelineStage {
    private final StreamPipeline pipelineToJoin;
    private final BFunctionPointer lhsKeyFunction;
    private final BFunctionPointer rhsKeyFunction;
    private final Map<String, List<Frame>> rhsFramesMap = new HashMap<>();
    private final Frame nilFrame;
    private BError failureAtJoin = null;
    private final Environment env;

    /**
     * Constructor for the OuterJoinClause.
     *
     * @param env The runtime environment.
     * @param pipelineToJoin The pipeline representing the right-hand side of the join.
     * @param lhsKeyFunction The function to extract the join key from the left-hand side.
     * @param rhsKeyFunction The function to extract the join key from the right-hand side.
     */
    public OuterJoinClause(Environment env, Object pipelineToJoin,
                           BFunctionPointer lhsKeyFunction, BFunctionPointer rhsKeyFunction) {
        this.pipelineToJoin = (StreamPipeline) pipelineToJoin;
        this.lhsKeyFunction = lhsKeyFunction;
        this.rhsKeyFunction = rhsKeyFunction;
        this.nilFrame = new Frame();
        this.env = env;
        initializeRhsFrames();
    }

    /**
     * Initializes the outer join clause.
     *
     * @param env The runtime environment.
     * @param pipelineToJoin The pipeline representing the right-hand side of the join.
     * @param lhsKeyFunction The function to extract the join key from the left-hand side.
     * @param rhsKeyFunction The function to extract the join key from the right-hand side.
     * @return The initialized OuterJoinClause.
     */
    public static OuterJoinClause initOuterJoinClause(Environment env, Object pipelineToJoin,
                                                      BFunctionPointer lhsKeyFunction, BFunctionPointer rhsKeyFunction) {
        return new OuterJoinClause(env, pipelineToJoin, lhsKeyFunction, rhsKeyFunction);
    }

    /**
     * Initializes the right-hand side (RHS) frames by processing the pipeline.
     */
    private void initializeRhsFrames() {
        try {
            Stream<Frame> strm = ((StreamPipeline)StreamPipeline.getStreamFromPipeline(pipelineToJoin)).getStream();
            strm.forEach(frame -> {
                Object key = rhsKeyFunction.call(env.getRuntime(), frame.getRecord());
                if (key instanceof BError) {
                    failureAtJoin = (BError) key;
                    return;
                }
                rhsFramesMap.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(frame);
            });
        } catch (QueryException e) {
            failureAtJoin = e.getError();
        }
    }

    /**
     * Executes the outer join by processing the left-hand side (LHS) frames.
     *
     * @param inputStream The input stream of frames (LHS).
     * @return A joined stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.flatMap(lhsFrame -> {
            try {
                if (failureAtJoin != null) {
                    throw new QueryException(failureAtJoin);
                }
                Object lhsKey = lhsKeyFunction.call(env.getRuntime(), lhsFrame.getRecord());
                if(lhsKey instanceof BError error) {
                    throw new QueryException(error);
                }
                List<Frame> rhsCandidates = rhsFramesMap.getOrDefault(lhsKey.toString(), Collections.emptyList());

                if (rhsCandidates.isEmpty()) {
                    // No matching RHS frames, join with nilFrame
                    Frame joinedFrame = new Frame();
                    lhsFrame.getRecord().entrySet().forEach(entry ->
                            joinedFrame.getRecord().put(entry.getKey(), entry.getValue())
                    );
                    nilFrame.getRecord().entrySet().forEach(entry ->
                            joinedFrame.getRecord().put(entry.getKey(), entry.getValue())
                    );
                    return Stream.of(joinedFrame);
                } else {
                    // Join with each matching RHS frame
                    return rhsCandidates.stream().map(rhsFrame -> mergeFrames(lhsFrame, rhsFrame));
                }
            } catch (BError e) {
                throw new QueryException(e);
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