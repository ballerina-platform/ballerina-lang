package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
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
        Map<Map<BString, Object>, List<Frame>> groupedData = inputStream
                .collect(Collectors.groupingBy(
                        this::extractProcessedKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedData.values().stream().map(frames -> {
            Frame firstFrame = frames.getFirst();
            Map<BString, Object> originalKey = extractOriginalKey(firstFrame);

            Frame groupedFrame = new Frame();
            BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

            originalKey.forEach(groupedRecord::put);

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

    private Map<BString, Object> extractOriginalKey(Frame frame) {
        Map<BString, Object> keyMap = new LinkedHashMap<>();
        BMap<BString, Object> record = frame.getRecord();

        for (int i = 0; i < groupingKeys.size(); i++) {
            BString key = (BString) groupingKeys.get(i);
            Object value;
            if (record.containsKey(key)) {
                value = record.get(key);
            } else {
                BMap rec = (BMap) record.get(VALUE_FIELD);
                value = rec.get(key);
            }
            keyMap.put(key, value);
        }
        return keyMap;
    }

    private Map<BString, Object> extractProcessedKey(Frame frame) {
        Map<BString, Object> keyMap = new LinkedHashMap<>();
        BMap<BString, Object> record = frame.getRecord();

        for (int i = 0; i < groupingKeys.size(); i++) {
            BString key = (BString) groupingKeys.get(i);
            Object value;
            if (record.containsKey(key)) {
                value = record.get(key);
            } else {
                BMap rec = (BMap) record.get(VALUE_FIELD);
                value = rec.get(key);
            }
            Object processedValue = processValue(value);
            keyMap.put(key, processedValue);
        }
        return keyMap;
    }

    private Object processValue(Object value) {
        if (value instanceof BArray array) {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                list.add(processValue(array.get(i)));
            }
            return list;
        } else if (value instanceof BMap) {
            BMap<BString, Object> bMap = (BMap<BString, Object>) value;
            Map<BString, Object> map = new LinkedHashMap<>();
            for (Map.Entry<BString, Object> entry : bMap.entrySet()) {
                map.put(entry.getKey(), processValue(entry.getValue()));
            }
            return map;
        } else {
            return value;
        }
    }
}
