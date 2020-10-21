/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerina.testobserve.tracing.extension;

import com.google.gson.Gson;
import io.ballerina.runtime.JSONParser;
import io.opentracing.mock.MockTracer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java functions called from Ballerina related to the mock tracer.
 */
public class MockTracerUtils {
    public static Object getFinishedSpans(String serviceName) {
        MockTracer mockTracer = BMockTracer.getTracerMap().get(serviceName);
        List<BMockSpan> mockSpans;
        if (mockTracer == null) {
            mockSpans = Collections.emptyList();
        } else {
            mockSpans = mockTracer.finishedSpans().stream()
                    .map(mockSpan -> new BMockSpan(mockSpan.operationName(),
                            mockSpan.context().traceId(),
                            mockSpan.context().spanId(),
                            mockSpan.parentId(),
                            mockSpan.tags()))
                    .collect(Collectors.toList());
        }
        return JSONParser.parse(new Gson().toJson(mockSpans));
    }
}
