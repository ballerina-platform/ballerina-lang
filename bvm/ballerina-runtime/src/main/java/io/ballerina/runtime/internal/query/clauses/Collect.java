/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a `collect` clause in the query pipeline.
 * It aggregates values based on non-grouping keys and applies a user-defined function.
 *
 * @since 2201.13.0
 */
public class Collect implements QueryClause {

    private final BArray nonGroupingKeys;
    private final BFunctionPointer collectFunc;
    private final Environment env;
    private final ArrayType arrayType = TypeCreator.createArrayType(TypeCreator.createUnionType(
            List.of(PredefinedTypes.TYPE_ANY, PredefinedTypes.TYPE_ERROR)));

    /**
     * Constructor for Collect.
     *
     * @param nonGroupingKeys The keys to collect values for.
     * @param collectFunc     The function applied to collected data.
     * @param env             The runtime environment.
     */
    private Collect(Environment env, BArray nonGroupingKeys, BFunctionPointer collectFunc) {
        this.nonGroupingKeys = nonGroupingKeys;
        this.collectFunc = collectFunc;
        this.env = env;
    }

    /**
     * Factory method for collect clause.
     *
     * @param nonGroupingKeys The keys to collect values for.
     * @param collectFunc     The function applied to collected data.
     * @param env             The runtime environment.
     * @return                A Collect instance.
     */
    public static Collect initCollectClause(Environment env,
                                            BArray nonGroupingKeys,
                                            BFunctionPointer collectFunc) {
        return new Collect(env, nonGroupingKeys, collectFunc);
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
        Frame groupedFrame = new Frame();
        BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

        for (int i = 0; i < nonGroupingKeys.size(); i++) {
            BString key = (BString) nonGroupingKeys.get(i);
            groupedRecord.put(key, new ArrayValueImpl(arrayType));
        }

        inputStream.forEach(frame -> {
            BMap<BString, Object> record = frame.getRecord();
            for (int i = 0; i < nonGroupingKeys.size(); i++) {
                BString key = (BString) nonGroupingKeys.get(i);
                if (record.containsKey(key) && record.get(key) != null) {
                    BArray existingValues = (BArray) groupedRecord.get(key);
                    existingValues.append(record.get(key));
                }
            }
        });

        return Stream.of(groupedFrame).map(frame -> {
            Object result = collectFunc.call(env.getRuntime(), groupedRecord);
            return switch (result) {
                case BError error -> throw error;
                case BMap<?, ?> map -> Frame.create((BMap<BString, Object>) map);
                default -> frame;
            };
        });
    }
}
