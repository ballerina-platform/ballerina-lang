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
 */

package org.ballerina.testobserve.tracing.extension;

import io.ballerina.runtime.observability.tracer.InvalidConfigurationException;
import io.ballerina.runtime.observability.tracer.OpenTracer;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracer extension that returns an instance of Mock tracer.
 */
public class BMockTracer implements OpenTracer {

    private static final PrintStream out = System.out;
    private static final Map<String, MockTracer> tracerMap = new ConcurrentHashMap<>();
    private static final String NAME = "BMockTracer";

    @Override
    public Tracer getTracer(String serviceName) {
        MockTracer mockTracer = new MockTracer();
        BMockTracer.tracerMap.put(serviceName, mockTracer);
        out.println("Initialized Mock Tracer for " + serviceName);
        return mockTracer;
    }

    @Override
    public void init() throws InvalidConfigurationException {
        // Do Nothing
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static Map<String, MockTracer> getTracerMap() {
        return tracerMap;
    }
}
