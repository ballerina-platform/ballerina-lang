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

import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.io.IOException;
import java.net.HttpURLConnection;
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

    private static BSpan start(Tracer tracer, Context parentContext, String operationName, boolean isClient) {
        SpanBuilder builder = tracer.spanBuilder(operationName);
        if (parentContext != null) {
            builder.setParent(parentContext);
        }
        builder.setAttribute(TraceConstants.TAG_KEY_SPAN_KIND, isClient
                ? TraceConstants.TAG_SPAN_KIND_CLIENT
                : TraceConstants.TAG_SPAN_KIND_SERVER);
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
     * @param httpExchange  Contains header information of the http request received
     * @param serviceName   The name of the service the span belongs to
     * @param operationName The name of the operation the span corresponds to
     * @param isClient      True if this is a client span
     * @return The new span
     */
    public static BSpan start(HttpExchange httpExchange, String serviceName, String operationName, boolean isClient) {

        TextMapGetter<HttpExchange> getter = new TextMapGetter<>() {
            @Override
            public String get(HttpExchange carrier, String key) {
                if (carrier != null && carrier.getRequestHeaders().containsKey(key)) {
                    return carrier.getRequestHeaders().get(key).get(0);
                }
                return null;
            }

            @Override
            public Iterable<String> keys(HttpExchange carrier) {
                return carrier.getRequestHeaders().keySet();
            }
        };

        Tracer tracer = TracersStore.getInstance().getTracer(serviceName);
        Context parentContext = TracersStore.getInstance().getPropagators()
                .getTextMapPropagator().extract(Context.current(), httpExchange, getter);
        return start(tracer, parentContext, operationName, isClient);
    }

    public void finishSpan() {
        span.end();
    }

    public void addEvent(Map<String, Attributes> fields) {
        fields.forEach(span::addEvent);
    }

    public void addTags(Map<String, String> tags) {
        tags.forEach(span::setAttribute);
    }

    public void addTag(String tagKey, String tagValue) {
        span.setAttribute(tagKey, tagValue);
    }

    public Map<String, String> extractContextAsHttpHeaders() {
        Map<String, String> carrierMap;
        if (span != null) {

            TextMapSetter<HttpURLConnection> setter = (carrier, key, value) -> {
                if (carrier != null) {
                    carrier.setRequestProperty(key, value);
                }
            };

            HttpURLConnection httpURLConnection = new HttpURLConnection(null) {
                @Override
                public void disconnect() {
                }

                @Override
                public boolean usingProxy() {
                    return false;
                }

                @Override
                public void connect() throws IOException {
                }
            };

            TracersStore.getInstance().getPropagators().getTextMapPropagator()
                    .inject(Context.current(), httpURLConnection, setter);
            carrierMap = new HashMap<>();
            httpURLConnection.getRequestProperties().forEach(
                    (k, strings) -> strings.forEach(v -> carrierMap.put(k, v))
            );
        } else {
            carrierMap = Collections.emptyMap();
        }
        return carrierMap;
    }
}
