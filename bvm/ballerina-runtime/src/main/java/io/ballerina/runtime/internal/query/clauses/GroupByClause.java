package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a `group by` clause in the query pipeline that processes a stream of frames.
 */
public class GroupByClause implements PipelineStage {

    private final BArray groupingKeys;
    private final BArray nonGroupingKeys;
    private final Environment env;

    /**
     * Constructor for the GroupByClause.
     *
     * @param groupingKeys The keys to group by.
     */
    public GroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        this.groupingKeys = groupingKeys;
        this.nonGroupingKeys = nonGroupingKeys;
        this.env = env;
    }

    public static GroupByClause initGroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        return new GroupByClause(env, groupingKeys, nonGroupingKeys);
    }

    /**
     * Groups a stream of frames by the specified grouping keys.
     *
     * @param inputStream The input stream of frames.
     * @return A grouped stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) throws BError {
        Map<Map<BString, Object>, List<Frame>> groupedData = inputStream
                .collect(Collectors.groupingBy(
                        frame -> extractGroupingKey(frame, groupingKeys),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedData.entrySet().stream().map(entry -> {
            Map<BString, Object> key = entry.getKey();
            List<Frame> frames = entry.getValue();

            Frame groupedFrame = new Frame();
            BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

            key.forEach(groupedRecord::put);

            for (int i = 0; i < nonGroupingKeys.size(); i++) {
                BString nonGroupingKey = (BString) nonGroupingKeys.get(i);

                Object[] values = frames.stream()
                        .map(f -> f.getRecord().get(nonGroupingKey))
                        .filter(Objects::nonNull)
                        .toArray();

                BArray valuesArray = new ArrayValueImpl(values, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
                groupedRecord.put(nonGroupingKey, valuesArray);
            }

            groupedFrame.updateRecord(groupedRecord);
            return groupedFrame;
        });
    }

    /**
     * Extracts the grouping key values from a frame.
     *
     * @param frame The frame to extract the keys from.
     * @param groupingKeys The keys to group by.
     * @return A map representing the grouping key values.
     */
    private Map<BString, Object> extractGroupingKey(Frame frame, BArray groupingKeys) {
        Map<BString, Object> keyMap = new LinkedHashMap<>();
        BMap<BString, Object> record = frame.getRecord();

        for (int i = 0; i < groupingKeys.size(); i++) {
            BString key = (BString) groupingKeys.get(i);
            if (record.containsKey(key)) {
                keyMap.put(key, record.get(key));
            } else {
                throw new RuntimeException("Missing key in record: " + key);
            }
        }

        return keyMap;
    }
}
