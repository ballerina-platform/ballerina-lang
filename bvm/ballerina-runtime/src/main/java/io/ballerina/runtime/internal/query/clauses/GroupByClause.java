package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;

public class GroupByClause implements PipelineStage {


    private final BArray groupingKeys;
    private final BArray nonGroupingKeys;
    private final Environment env;

    public GroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        this.groupingKeys = groupingKeys;
        this.nonGroupingKeys = nonGroupingKeys;
        this.env = env;
    }

    public static GroupByClause initGroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        return new GroupByClause(env, groupingKeys, nonGroupingKeys);
    }

    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        Map<GroupKey, List<Frame>> groupedData = inputStream
                .collect(Collectors.groupingBy(
                        frame -> new GroupKey(extractOriginalKey(frame)),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedData.values().stream().map(frames -> {
            Frame firstFrame = frames.getFirst();
            BMap<BString, Object> originalKey = extractOriginalKey(firstFrame);

            Frame groupedFrame = new Frame();
            BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

            // Copy original key fields to the grouped record
            originalKey.entrySet().forEach(entry -> groupedRecord.put(entry.getKey(), entry.getValue()));

            // Aggregate non-grouping fields into arrays
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

    private BMap<BString, Object> extractOriginalKey(Frame frame) {
        BMap<BString, Object> keyMap = ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_ANY));
        BMap<BString, Object> record = frame.getRecord();

        for (int i = 0; i < groupingKeys.size(); i++) {
            BString key = (BString) groupingKeys.get(i);
            Object value;

            if (record.containsKey(key)) {
                value = record.get(key);
            } else {
                BMap<BString, Object> nestedRec = (BMap<BString, Object>) record.get(VALUE_FIELD);
                value = nestedRec.get(key);
            }

            keyMap.put(key, value);
        }
        return keyMap;
    }

    // Custom key wrapper for deep equality grouping
    private static class GroupKey {
        private final BMap<BString, Object> keyMap;

        GroupKey(BMap<BString, Object> keyMap) {
            this.keyMap = keyMap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GroupKey)) return false;
            GroupKey other = (GroupKey) o;
            return TypeChecker.isEqual(this.keyMap, other.keyMap);
        }

        @Override
        public int hashCode() {
            return 36;
        }
    }
}
