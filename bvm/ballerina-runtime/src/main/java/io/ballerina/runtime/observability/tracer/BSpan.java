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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code BSpan} holds the trace of the current context.
 *
 * @since 0.964.1
 */
public class BSpan {
    private final Tracer tracer;
    private final Span span;

    private BSpan(Tracer tracer, Span span) {
        this.tracer = tracer;
        this.span = span;
    }

    private static BSpan start(Tracer tracer, SpanContext parentSpanContext, String operationName, boolean isClient) {
        Span span = tracer.buildSpan(operationName)
                .asChildOf(parentSpanContext)
                .withTag(TraceConstants.TAG_KEY_SPAN_KIND, isClient
                        ? TraceConstants.TAG_SPAN_KIND_CLIENT
                        : TraceConstants.TAG_SPAN_KIND_SERVER)
                .start();
        return new BSpan(tracer, span);
    }

    /**
     * Start a new span without a parent. The started span will be a root span.
     *
     * @param serviceName   The name of the service the span belongs to
     * @param operationName The name of the operation the span corresponds to
     * @param isClient      True if this is a client span
     * @return The new span
     */
    public static BSpan start(String serviceName, String operationName, boolean isClient) {
        Tracer tracer = TracersStore.getInstance().getTracer(serviceName);
        return start(tracer, null, operationName, isClient);
    }

    /**
     * Start a new span with a parent using parent span.
     *
     * @param parentSpan    The parent span of the new span
     * @param serviceName   The name of the service the span belongs to
     * @param operationName The name of the operation the span corresponds to
     * @param isClient      True if this is a client span
     * @return The new span
     */
    public static BSpan start(BSpan parentSpan, String serviceName, String operationName, boolean isClient) {
        Tracer tracer = TracersStore.getInstance().getTracer(serviceName);
        return start(tracer, parentSpan.span.context(), operationName, isClient);
    }

    /**
     * Start a new span with a parent using parent trace context.
     * The started span is part of a trace which had spanned across multiple services and the parent is in the service
     * which called the current service.
     *
     * @param parentTraceContext The parent trace context
     * @param serviceName        The name of the service the span belongs to
     * @param operationName      The name of the operation the span corresponds to
     * @param isClient           True if this is a client span
     * @return The new span
     */
    public static BSpan start(Map<String, String> parentTraceContext, String serviceName, String operationName,
                              boolean isClient) {
        Tracer tracer = TracersStore.getInstance().getTracer(serviceName);
        SpanContext parentSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                new TextMapExtractAdapter(parentTraceContext));
        return start(tracer, parentSpanContext, operationName, isClient);
    }

    public void finishSpan() {
        span.finish();
    }

    public void addEvent(Map<String, Object> fields) {
        span.log(fields);
    }

    public void addTags(Map<String, String> tags) {
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            span.setTag(entry.getKey(), entry.getValue());
        }
    }

    public void addTag(String tagKey, String tagValue) {
        span.setTag(tagKey, tagValue);
    }

    public Map<String, String> extractContextAsHttpHeaders() {
        Map<String, String> carrierMap;
        if (span != null) {
            carrierMap = new HashMap<>();
            TextMapInjectAdapter requestInjector = new TextMapInjectAdapter(carrierMap);
            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, requestInjector);
        } else {
            carrierMap = Collections.emptyMap();
        }
        return carrierMap;
    }
}
