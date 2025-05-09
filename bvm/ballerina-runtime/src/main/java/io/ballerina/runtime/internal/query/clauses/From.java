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

/**
 * Represents a `from` clause in the query pipeline that processes a stream of frames.
 *
 * @since 2201.13.0
 */
public class From implements QueryClause {

    private final BFunctionPointer transformer;
    private final Environment env;

    /**
     * Constructor for the From.
     *
     * @param transformer The function to transform each frame.
     */
    private From(Environment env, BFunctionPointer transformer) {
        this.transformer = transformer;
        this.env = env;
    }

    public static From initFromClause(Environment env, BFunctionPointer transformer) {
        return new From(env, transformer);
    }

    /**
     * Processes a stream of frames by applying the transformation function to each frame.
     *
     * @param inputStream The input stream of frames.
     * @return A transformed stream of frames.
     */
    @Override
    public Stream<BMap<BString, Object>> process(Stream<BMap<BString, Object>> inputStream) {
        return inputStream.map(frame -> {
            Object result = transformer.call(env.getRuntime(), frame);
            if (result instanceof BMap<?, ?> map) {
                return (BMap<BString, Object>) map;
            }
            throw new QueryException((BError) result);
        });
    }
}
