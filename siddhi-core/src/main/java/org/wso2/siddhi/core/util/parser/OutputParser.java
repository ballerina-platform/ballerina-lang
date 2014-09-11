/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.rateLimit.PassThroughOutputRateLimiter;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

public class OutputParser {


    public static OutputCallback constructOutputCallback(OutputStream outStream, ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                                         StreamDefinition outputStreamDefinition, SiddhiContext siddhiContext) {
        String id = outStream.getId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            StreamJunction outputStreamJunction = streamJunctionMap.get(id);
            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(outputStreamDefinition, (ExecutorService) siddhiContext.getExecutorService(), siddhiContext.getDefaultEventBufferSize());
                streamJunctionMap.putIfAbsent(id, outputStreamJunction);
            }
            return new InsertIntoStreamCallback(outputStreamJunction, outputStreamDefinition);

        } else {
            throw new QueryCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputCallback constructOutputCallback(OutputStream outStream, String key, ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                                         StreamDefinition outputStreamDefinition, SiddhiContext siddhiContext) {
        String id = outStream.getId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            StreamJunction outputStreamJunction = streamJunctionMap.get(id + key);
            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(outputStreamDefinition, (ExecutorService) siddhiContext.getExecutorService(), siddhiContext.getDefaultEventBufferSize());
                streamJunctionMap.putIfAbsent(id + key, outputStreamJunction);
            }
            return new InsertIntoStreamCallback(outputStreamJunction, outputStreamDefinition);

        } else {
            throw new QueryCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputRateLimiter constructOutputRateLimiter(String id, OutputRate outputRate) {
        if (outputRate == null) {
            return new PassThroughOutputRateLimiter(id);
        }
        //TODO: else
        return null;
    }
}
