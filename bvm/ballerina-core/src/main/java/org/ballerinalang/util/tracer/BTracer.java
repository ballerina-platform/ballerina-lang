/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.tracer;

import io.opentracing.Tracer;

import java.util.List;

/**
 * {@code BTracer} holds a openTracer and other relevant data required for tracing.
 */
public class BTracer {
    private final Tracer openTracer;
    private final List<String> tracingHeaders;

    public BTracer(Tracer openTracer, List<String> tracingHeaders) {
        this.openTracer = openTracer;
        this.tracingHeaders = tracingHeaders;
    }

    public Tracer getOpenTracer() {
        return openTracer;
    }

    public List<String> getTracingHeaders() {
        return tracingHeaders;
    }
}
