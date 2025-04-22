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
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.stream.Stream;

/**
 * Represents a `where` clause in the query pipeline that filters a stream of frames.
 *
 * @since 2201.13.0
 */
public class Where implements QueryClause {
    private final BFunctionPointer filterFunc;
    private final Environment env;

    /**
     * Constructor for the Where.
     *
     * @param env        The runtime environment.
     * @param filterFunc The function to filter frames.
     */
    private Where(Environment env, BFunctionPointer filterFunc) {
        this.filterFunc = filterFunc;
        this.env = env;
    }

    /**
     * Static initializer for Where.
     *
     * @param env        The runtime environment.
     * @param filterFunc The filter function.
     * @return A new instance of Where.
     */
    public static Where initWhereClause(Environment env, BFunctionPointer filterFunc) {
        return new Where(env, filterFunc);
    }

    /**
     * Filters a stream of frames by applying the filter function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A filtered stream of frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.filter(frame -> {
            Object result = filterFunc.call(env.getRuntime(), frame.getRecord());
            if (result instanceof Boolean booleanValue) {
                return booleanValue;
            }
            throw new QueryException((BError) result);
        });
    }
}
