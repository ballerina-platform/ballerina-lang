package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.util.stream.Stream;

/**
 * Represents a `collect` clause in the query pipeline.
 * It aggregates values based on non-grouping keys and applies a user-defined function.
 */
public class CollectClause implements PipelineStage {

    private final BArray nonGroupingKeys;
    private final BFunctionPointer collectFunc;
    private final Environment env;

    /**
     * Constructor for CollectClause.
     *
     * @param nonGroupingKeys The keys to collect values for.
     * @param collectFunc     The function applied to collected data.
     * @param env             The runtime environment.
     */
    public CollectClause(Environment env, BArray nonGroupingKeys, BFunctionPointer collectFunc) {
        this.nonGroupingKeys = nonGroupingKeys;
        this.collectFunc = collectFunc;
        this.env = env;
    }

    public static CollectClause initCollectClause(Environment env, BArray nonGroupingKeys, BFunctionPointer collectFunc) {
        return new CollectClause(env, nonGroupingKeys, collectFunc);
    }

    /**
     * Processes a stream of frames by aggregating values for `nonGroupingKeys`
     * and applying `collectFunc` on the grouped frame.
     *
     * @param inputStream The input stream of frames.
     * @return A transformed stream with the collected frame.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        try {
            return inputStream.map(frame -> {
                Frame groupedFrame = new Frame();
                BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

                for (int i = 0; i < nonGroupingKeys.size(); i++) {
                    BString key = (BString) nonGroupingKeys.get(i);
                    groupedRecord.put(key, new ArrayValueImpl(TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY)));
                }

                BMap<BString, Object> record = frame.getRecord();
                for (int i = 0; i < nonGroupingKeys.size(); i++) {
                    BString key = (BString) nonGroupingKeys.get(i);
                    if (record.containsKey(key)) {
                        BArray existingValues = (BArray) groupedRecord.get(key);
                        existingValues.append(record.get(key));
                    }
                }

                Object result = collectFunc.call(env.getRuntime(), groupedRecord);
                if (result instanceof BMap) {
                    groupedFrame.updateRecord((BMap<BString, Object>) result);
                    return groupedFrame;
                } else if (result instanceof Frame) {
                    return (Frame) result;
                } else {
                    throw new RuntimeException("Collect function returned an unexpected type: " + result.getClass().getName());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error applying collect function: " + e.getMessage(), e);
        }
    }
}
