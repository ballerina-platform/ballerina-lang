/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for
 *  the specific language governing permissions and limitations
 *  under the License.
 *
 */

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;

/**
 * Represents a `group by` clause in the query pipeline that processes a stream of frames.
 *
 * @since 2201.13.0
 */
public class GroupByClause implements PipelineStage {

    private final BArray groupingKeys;
    private final BArray nonGroupingKeys;
    private final Environment env;

    private GroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
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

        return groupedData.values().stream().map(this::aggregateNonGroupingKeys);
    }

    private Frame aggregateNonGroupingKeys(List<Frame> frames) {
        Frame groupedFrame = frames.getFirst();
        BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

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
    private record GroupKey(BMap<BString, Object> keyMap) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GroupKey other)) {
                return false;
            }
            return TypeChecker.isEqual(this.keyMap, other.keyMap);
        }

        @Override
        public int hashCode() {
            return 36;
        }
    }
}
