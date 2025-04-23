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
import io.ballerina.runtime.internal.query.utils.QueryException;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.LIMIT_NEGATIVE_ERROR;

/**
 * Represents a `limit` clause in the query pipeline that restricts the number of frames.
 *
 * @since 2201.13.0
 */
public class Limit implements QueryClause {
    private final BFunctionPointer limitFunction;
    private final Environment env;

    /**
     * Constructor for the Limit.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     */
    private Limit(Environment env, BFunctionPointer limitFunction) {
        this.limitFunction = limitFunction;
        this.env = env;
    }

    /**
     * Static initializer for Limit.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     * @return A new instance of Limit.
     */
    public static Limit initLimitClause(Environment env, BFunctionPointer limitFunction) {
        return new Limit(env, limitFunction);
    }

    /**
     * Processes a stream of frames by applying the limit function to determine the maximum number of frames.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of frames with at most `limit` frames.
     */
    @Override
    public Stream<BMap<BString, Object>> process(Stream<BMap<BString, Object>> inputStream) {
        BMap<BString, Object> record = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame");
        Object limitResult = limitFunction.call(env.getRuntime(), record);
        if (limitResult instanceof BError error) {
            return Stream.generate(() -> {
                throw new QueryException(error);
            });
        }
        Long limit = (Long) limitResult;
        if (limit < 1) {
            throw new ErrorValue(LIMIT_NEGATIVE_ERROR);
        }
        return inputStream.limit(limit);
    }
}
