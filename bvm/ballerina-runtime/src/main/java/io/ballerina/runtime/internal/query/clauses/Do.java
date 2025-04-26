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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.stream.Stream;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_ACCESS_FIELD;

/**
 * Represents a `do` clause in the query pipeline that applies a function to each element.
 *
 * @since 2201.13.0
 */
public class Do implements QueryClause {
    private final Environment env;
    private final BFunctionPointer function;

    /**
     * Constructor for the Do.
     *
     * @param env The runtime environment.
     * @param function The function to be executed for each frame.
     */
    private Do(Environment env, BFunctionPointer function) {
        this.env = env;
        this.function = function;
    }

    public static Do initDoClause(Environment env, BFunctionPointer function) {
        return new Do(env, function);
    }

    /**
     * Processes each element in the stream by applying the function.
     *
     * @param inputStream The input stream of frames.
     * @return The same stream after applying the function.
     */
    @Override
    public Stream<BMap<BString, Object>> process(Stream<BMap<BString, Object>> inputStream) {
        return inputStream.map(frame -> {
            Object result = function.call(env.getRuntime(), frame);
            if (result instanceof BError error) {
                throw new QueryException(error);
            }
            if (result != null) {
                frame.put(VALUE_ACCESS_FIELD, result);
            }
            return frame;
        });
    }
}
