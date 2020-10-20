/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.observability.tracer;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link TraceManager} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.964.1
 */
public class TraceManager {
    private static final TraceManager instance = new TraceManager();
    private final TracersStore tracerStore;

    private TraceManager() {
        tracerStore = TracersStore.getInstance();
    }

    public static TraceManager getInstance() {
        return instance;
    }

    public void startSpan(BSpan parentBSpan, BSpan activeBSpan) {
        if (activeBSpan != null) {
            String service = activeBSpan.getServiceName();
            String operationName = activeBSpan.getOperationName();

            Span span;
            if (parentBSpan != null) {
                span = startSpan(operationName, parentBSpan.getSpan(), activeBSpan.getTags(), service, false);
            } else {
                span = startSpan(operationName, extractSpanContext(activeBSpan.getProperties(), service),
                        activeBSpan.getTags(), service, true);
            }

            activeBSpan.setSpan(span);
        }
    }

    public void finishSpan(BSpan bSpan) {
        bSpan.getSpan().finish();
    }

    public void log(BSpan bSpan, Map<String, Object> fields) {
        bSpan.getSpan().log(fields);
    }

    public void addTags(BSpan bSpan, Map<String, String> tags) {
        tags.forEach((key, value) -> bSpan.getSpan().setTag(key, String.valueOf(value)));
    }

    public Map<String, String> extractTraceContext(Span span, String serviceName) {
        Map<String, String> carrierMap = new HashMap<>();
        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer != null && span != null) {
            TextMapInjectAdapter requestInjector = new TextMapInjectAdapter(carrierMap);
            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, requestInjector);
        }
        return carrierMap;
    }

    private Span startSpan(String spanName, Object spanContextMap,
                           Map<String, String> tags, String serviceName, boolean isParent) {
        Tracer tracer = tracerStore.getTracer(serviceName);
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);

        for (Map.Entry<String, String> tag : tags.entrySet()) {
            spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
        }

        if (spanContextMap != null) {
            if (isParent) {
                spanBuilder = spanBuilder.asChildOf((SpanContext) spanContextMap);
            } else {
                spanBuilder = spanBuilder.asChildOf((Span) spanContextMap);
            }
        }
        return spanBuilder.start();
    }

    private Object extractSpanContext(Map<String, String> traceContext, String serviceName) {
        SpanContext spanContext = null;
        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer != null) {
            spanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(traceContext));
        }
        return spanContext;
    }

}
