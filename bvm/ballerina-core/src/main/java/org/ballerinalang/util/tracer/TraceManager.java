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
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.config.ConfigRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;
import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX;

/**
 * {@link TraceManager} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.964.1
 */
public class TraceManager {
    private static final TraceManager instance = new TraceManager();
    private TracersStore tracerStore;
    private Stack<BSpan> bSpanStack;

    private TraceManager() {
        Map<String, String> configurations = ConfigRegistry.getInstance().getConfigTable(CONFIG_TABLE_TRACING);
        tracerStore = new TracersStore(configurations);
        bSpanStack = new Stack<>();
    }

    public static TraceManager getInstance() {
        return instance;
    }

    public void startSpan(WorkerExecutionContext ctx) {
        BSpan activeBSpan = TraceUtil.getBSpan(ctx);
        if (activeBSpan != null) {
            BSpan parentBSpan = !bSpanStack.empty() ? bSpanStack.peek() : null;
            String service = activeBSpan.getConnectorName();
            String resource = activeBSpan.getActionName();

            Map<String, Span> spanList;
            if (parentBSpan != null) {
                spanList = startSpan(resource, parentBSpan.getSpans(),
                        activeBSpan.getTags(), service, false);
            } else {
                Map<String, String> spanHeaders = activeBSpan.getProperties()
                        .entrySet().stream().collect(
                                Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))
                        );
                spanList = startSpan(resource, extractSpanContext(spanHeaders, service),
                        activeBSpan.getTags(), service, true);
            }

            activeBSpan.setSpans(spanList);
            bSpanStack.push(activeBSpan);
        }
    }

    public void finishSpan(BSpan bSpan) {
        bSpan.getSpans().values().forEach(Span::finish);
        if (!bSpanStack.empty()) {
            bSpanStack.pop();
        }
    }

    public void log(BSpan bSpan, Map<String, Object> fields) {
        bSpan.getSpans().values().forEach(span -> span.log(fields));
    }

    public void addTags(BSpan bSpan, Map<String, String> tags) {
        bSpan.getSpans().values().forEach(span -> {
            tags.forEach((key, value) -> span.setTag(key, String.valueOf(value)));
        });
    }

    public BSpan getParentBSpan() {
        return !bSpanStack.empty() ? bSpanStack.peek() : null;
    }

    public Map<String, String> extractTraceContext(Map<String, Span> activeSpanMap, String serviceName) {
        Map<String, String> carrierMap = new HashMap<>();
        for (Map.Entry<String, Span> activeSpanEntry : activeSpanMap.entrySet()) {
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
            Tracer tracer = tracers.get(activeSpanEntry.getKey());
            if (tracer != null) {
                Span span = activeSpanEntry.getValue();
                if (span != null) {
                    tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,
                            new RequestInjector(TRACE_PREFIX, carrierMap));
                }
            }
        }
        return carrierMap;
    }

    private Map<String, Span> startSpan(String spanName, Map<String, ?> spanContextMap,
                                        Map<String, String> tags, String serviceName, boolean isParent) {
        Map<String, Span> spanMap = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry spanContextEntry : spanContextMap.entrySet()) {
            Tracer tracer = tracers.get(spanContextEntry.getKey().toString());
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);

            for (Map.Entry<String, String> tag : tags.entrySet()) {
                spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
            }

            if (spanContextEntry.getValue() != null) {
                if (isParent) {
                    spanBuilder = spanBuilder.asChildOf((SpanContext) spanContextEntry.getValue());
                } else {
                    spanBuilder = spanBuilder.asChildOf((Span) spanContextEntry.getValue());
                }
            }

            Span span = spanBuilder.start();
            spanMap.put(spanContextEntry.getKey().toString(), span);
        }
        return spanMap;
    }

    private Map<String, Object> extractSpanContext(Map<String, String> headers, String serviceName) {
        Map<String, Object> spanContext = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
            spanContext.put(tracerEntry.getKey(), tracerEntry.getValue()
                    .extract(Format.Builtin.HTTP_HEADERS, new RequestExtractor(headers)));
        }
        return spanContext;
    }

}
