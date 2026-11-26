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

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code BSpan} holds the trace of the current context.
 *
 * @since 0.964.1
 */
public class BSpan {
    private final Tracer tracer;
    private final Span span;
    private BMap<BString, Object> bSpanContext;
    private static final MapType IMMUTABLE_STRING_MAP_TYPE = TypeCreator.createMapType(
            PredefinedTypes.TYPE_STRING, true);

    private static final PropagatingParentContextGetter GETTER = new PropagatingParentContextGetter();
    private static final PropagatingParentContextSetter SETTER = new PropagatingParentContextSetter();

    static class PropagatingParentContextGetter implements TextMapGetter<Map<String, String>> {
        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
        }

        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }
    }

    static class PropagatingParentContextSetter implements TextMapSetter<Map<String, String>> {
        @Override
        public void set(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
        }
    }

    private BSpan(Tracer tracer, Span span) {
        this.tracer = tracer;
        this.span = span;
    }

    private static BSpan start(Tracer tracer, Context parentContext, String operationName, boolean isClient) {
        SpanBuilder builder = tracer.spanBuilder(operationName);
        if (parentContext != null) {
            builder.setParent(parentContext);
        }
        builder.setAttribute(TraceConstants.TAG_KEY_SPAN_KIND, isClient
                ? TraceConstants.TAG_SPAN_KIND_CLIENT
                : TraceConstants.TAG_SPAN_KIND_SERVER);
        builder.setSpanKind(isClient ? SpanKind.CLIENT : SpanKind.SERVER);
        Span span = builder.startSpan();
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
        Context parentContext = Context.current().with(parentSpan.span);
        return start(tracer, parentContext, operationName, isClient);
    }

    /**
     * Start a new span with a parent using parent trace context.
     * The started span is part of a trace which had spanned across multiple services and the parent is in the service
     * which called the current service.
     *
     * @param parentTraceContext Contains http headers of request received
     * @param serviceName        The name of the service the span belongs to
     * @param operationName      The name of the operation the span corresponds to
     * @param isClient           True if this is a client span
     * @return The new span
     */
    public static BSpan start(Map<String, String> parentTraceContext, String serviceName, String operationName,
                              boolean isClient) {

        Tracer tracer = TracersStore.getInstance().getTracer(serviceName);
        Context parentContext = TracersStore.getInstance().getPropagators()
                .getTextMapPropagator().extract(Context.current(), parentTraceContext, GETTER);
        return start(tracer, parentContext, operationName, isClient);
    }

    public void finishSpan() {
        span.end();
    }

    public void addEvent(String eventName, Attributes attributes) {
        span.addEvent(eventName, attributes);
    }

    public void setStatus(StatusCode statusCode) {
        span.setStatus(statusCode);
    }

    public void addTags(Map<String, String> tags) {
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            span.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void addTag(String tagKey, String tagValue) {
        span.setAttribute(tagKey, tagValue);
    }

    /**
     * Add a link from this span to another span, without making it the parent.
     *
     * @param linkedTraceContext Trace context headers of the linked span
     */
    public void addLink(Map<String, String> linkedTraceContext) {
        addLink(linkedTraceContext, Attributes.empty());
    }

    /**
     * Add a link from this span to another span, without making it the parent.
     *
     * @param linkedTraceContext Trace context headers of the linked span
     * @param attributes         Attributes of the link
     */
    public void addLink(Map<String, String> linkedTraceContext, Map<String, String> attributes) {
        AttributesBuilder linkAttributes = Attributes.builder();
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                linkAttributes.put(entry.getKey(), entry.getValue());
            }
        }
        addLink(linkedTraceContext, linkAttributes.build());
    }

    /**
     * Add a link from this span to another span, without making it the parent.
     *
     * @param linkedTraceContext Trace context headers of the linked span
     * @param attributes         Attributes of the link
     */
    public void addLink(Map<String, String> linkedTraceContext, Attributes attributes) {
        if (linkedTraceContext == null || linkedTraceContext.isEmpty()) {
            return;
        }
        Map<String, String> carrier = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, String> entry : linkedTraceContext.entrySet()) {
            if (entry.getKey() != null) {
                carrier.put(entry.getKey(), entry.getValue());
            }
        }
        Context linkedContext = TracersStore.getInstance().getPropagators()
                .getTextMapPropagator().extract(Context.root(), carrier, GETTER);
        SpanContext linkedSpanContext = Span.fromContext(linkedContext).getSpanContext();
        if (!linkedSpanContext.isValid()) {
            return;
        }
        span.addLink(linkedSpanContext, attributes != null ? attributes : Attributes.empty());
    }

    public Map<String, String> extractContextAsHttpHeaders() {

        Map<String, String> carrierMap;
        if (span != null) {
            carrierMap = new HashMap<>();
            TextMapPropagator propagator = TracersStore.getInstance().getPropagators().getTextMapPropagator();
            propagator.inject(Context.current().with(span), carrierMap, SETTER);
        } else {
            carrierMap = Collections.emptyMap();
        }
        return carrierMap;
    }

    public BMap<BString, Object> getBSpanContext() {

        if (bSpanContext == null) {
            SpanContext spanContext = span.getSpanContext();
            BMapInitialValueEntry[] values = new BMapInitialValueEntry[]{
                    new MappingInitialValueEntry.KeyValueEntry(
                            TraceConstants.SPAN_CONTEXT_MAP_KEY_TRACE_ID,
                            StringUtils.fromString(spanContext.getTraceId())),
                    new MappingInitialValueEntry.KeyValueEntry(
                            TraceConstants.SPAN_CONTEXT_MAP_KEY_SPAN_ID,
                            StringUtils.fromString(spanContext.getSpanId()))
            };
            bSpanContext = ValueCreator.createMapValue(IMMUTABLE_STRING_MAP_TYPE, values);
        }
        return bSpanContext;
    }
}
