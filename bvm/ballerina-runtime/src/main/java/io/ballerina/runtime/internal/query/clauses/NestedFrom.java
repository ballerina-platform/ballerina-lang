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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.utils.IteratorUtils;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;

/**
 * Represents a nested `from` clause in the query pipeline that processes a stream of frames.
 *
 * @since 2201.13.0
 */
public class NestedFrom implements QueryClause {
    private final BFunctionPointer collectionFunc;
    private final Environment env;

    /**
     * Constructor for the NestedFrom.
     *
     * @param env The runtime environment.
     * @param collectionFunc The function to extract the collection from each frame.
     */
    private NestedFrom(Environment env, BFunctionPointer collectionFunc) {
        this.collectionFunc = collectionFunc;
        this.env = env;
    }

    public static NestedFrom initNestedFromClause(Environment env, BFunctionPointer collectionFunc) {
        return new NestedFrom(env, collectionFunc);
    }

    /**
     * Processes a stream of frames by applying the collection function and iterating over the resulting collection.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of frames from the nested collections.
     */
    @Override
    public Stream<BMap<BString, Object>> process(Stream<BMap<BString, Object>> inputStream) {
        return inputStream.flatMap(frame -> {
            Object collection = collectionFunc.call(env.getRuntime(), frame);
            if (collection instanceof BError error) {
                throw new QueryException(error);
            }

            if (collection == null) {
                return Stream.empty();
            }

            Iterator<?> itr = IteratorUtils.getIterator(env, collection);
            List<BMap<BString, Object>> results = new ArrayList<>();

            try {
                while (itr.hasNext()) {
                    Object item = itr.next();
                    BMap<BString, Object> newRecord = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame");
                    frame.entrySet().forEach(entry -> {
                        BString key = entry.getKey();
                        Object value = entry.getValue();
                        newRecord.put(key, value);
                    });
                    newRecord.put(VALUE_FIELD, item);
                    results.add(newRecord);
                }
            } catch (BError e) {
                throw new QueryException(e, true);
            }
            return results.stream();
        });
    }
}
