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

package org.ballerinalang.util.tracer;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold the spans of a trace when using open tracing ballerina api.
 */
class SpanStore {

    // Map(Span ID -> Map(TracerName -> Span))
    private Map<String, Map<String, Span>> spans;
    private Map<String, Map<String, SpanContext>> spanContexts;

    SpanStore() {
        this.spans = new HashMap<>();
        this.spanContexts = new HashMap<>();
    }

    Map<String, Span> getSpan(String spanId) {
        return spans.get(spanId);
    }

    Map<String, SpanContext> getSpanContext(String spanId) {
        return spanContexts.get(spanId);
    }

    Map getParent(String spanId) {
        return (spanContexts.get(spanId) == null) ? spans.get(spanId) : spanContexts.get(spanId);
    }

    void addSpan(String spanId, Map<String, Span> spanMap) {
        spans.put(spanId, spanMap);
    }

    Map<String, Span> closeSpan(String spanId) {
        spanContexts.remove(spanId);
        return spans.remove(spanId);
    }

    public void addSpanContext(String spanId, Map<String, SpanContext> spanContextMap) {
        spanContexts.put(spanId, spanContextMap);
    }
}
