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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.stream.Stream;

/**
 * Represents a `on conflict` clause in the query pipeline that processes a stream of frames.
 *
 * @since 2201.13.0
 */
public class OnConflictClause implements PipelineStage {
    private final BFunctionPointer onConflictFunction;
    private final Environment env;

    private OnConflictClause(Environment env, BFunctionPointer onConflictFunction) {
        this.onConflictFunction = onConflictFunction;
        this.env = env;
    }

    public static OnConflictClause initOnConflictClause(Environment env, BFunctionPointer onConflictFunction) {
        return new OnConflictClause(env, onConflictFunction);
    }


    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.map(frame -> {
            Object result = onConflictFunction.call(env.getRuntime(), frame.getRecord());
            if (result instanceof BMap) {
                frame.updateRecord((BMap<BString, Object>) result);
                return frame;
            } else {
                throw new QueryException((BError) result);
            }
        });
    }
}
