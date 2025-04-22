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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;
import io.ballerina.runtime.internal.utils.ValueComparisonUtils;

import java.util.Comparator;
import java.util.stream.Stream;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.ORDER_DIRECTION;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.ORDER_KEY;

/**
 * Represents a `order by` clause in the query pipeline that processes a stream of frames.
 *
 * @since 2201.13.0
 */
public class OrderBy implements QueryClause {
    private final BFunctionPointer orderKeyFunction;
    private final Environment env;

    private OrderBy(Environment env, BFunctionPointer orderKeyFunction) {
        this.orderKeyFunction = orderKeyFunction;
        this.env = env;
    }

    public static OrderBy initOrderByClause(Environment env, BFunctionPointer orderKeyFunction) {
        return new OrderBy(env, orderKeyFunction);
    }

    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.peek(frame -> {
            Object result = orderKeyFunction.call(env.getRuntime(), frame.getRecord());
            if (result instanceof BError error) {
                throw new QueryException(error);
            }
        }).sorted(getComparator());
    }

    private Comparator<Frame> getComparator() {
        return (frame1, frame2) -> {
            BMap<BString, Object> record1 = frame1.getRecord();
            BMap<BString, Object> record2 = frame2.getRecord();

            BArray orderKey1Array = (BArray) record1.get(ORDER_KEY);
            BArray orderKey2Array = (BArray) record2.get(ORDER_KEY);
            BArray orderDirectionArray = (BArray) record1.get(ORDER_DIRECTION);

            int size = orderKey1Array.size();
            for (int i = 0; i < size; i++) {
                Object key1 = orderKey1Array.getRefValue(i);
                Object key2 = orderKey2Array.getRefValue(i);
                boolean ascending = orderDirectionArray.getBoolean(i);

                // Fix direction parameter
                String direction = ascending ? "ascending" : "descending";
                int comparison = ValueComparisonUtils.compareValues(key1, key2, direction);

                if (comparison != 0) {
                    return ascending ? comparison : -comparison;
                }
            }
            return 0;
        };
    }
}
