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
import org.ballerinalang.util.tracer.config.ConfigLoader;
import org.ballerinalang.util.tracer.config.OpenTracingConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX;
import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX_LENGTH;

/**
 * {@link TraceManager} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.964.1
 */
public class TraceManager {
    private static final TraceManager instance = new TraceManager();
    private TracersStore tracerStore;
    private boolean enabled;

    private TraceManager() {
        OpenTracingConfig openTracingConfig = ConfigLoader.load();
        if (openTracingConfig != null) {
            enabled = !Boolean.valueOf(ConfigRegistry.getInstance()
                    .getConfiguration(TraceConstants.DISABLE_OBSERVE_KEY));
            tracerStore = enabled ? new TracersStore(openTracingConfig) : null;
        }
    }

    public static TraceManager getInstance() {
        return instance;
    }

    public boolean isTraceEnabled() {
        return enabled;
    }

    public void startSpan(WorkerExecutionContext ctx) {
        if (enabled) {
            BTracer activeTracer = TraceUtil.getTracer(ctx);
            if (activeTracer != null) {
                BTracer parentTracer = TraceUtil.getParentTracer(ctx.parent);

                String service = activeTracer.getConnectorName();
                String resource = activeTracer.getActionName();

                // get the parent spans' span context
                parentTracer = (parentTracer != null) ? parentTracer : activeTracer;
                Map<String, String> spanHeaders = parentTracer.getProperties().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

                Map<String, Object> spanContext = extract(removeTracePrefix(spanHeaders), service);
                Map<String, Span> spanList = startSpan(resource, spanContext, activeTracer.getTags(), service);
                Map<String, String> traceContextMap = inject(spanList, service);

                activeTracer.getProperties().putAll(traceContextMap);
                activeTracer.setSpans(spanList);
            }
        }
    }

    public void finishSpan(BTracer bTracer) {
        if (enabled) {
            bTracer.getSpans().values().forEach(Span::finish);
        }
    }

    public void log(BTracer bTracer, Map<String, Object> fields) {
        if (enabled) {
            bTracer.getSpans().values().forEach(span -> span.log(fields));
        }
    }

    public void addTags(BTracer bTracer, Map<String, String> tags) {
        if (enabled) {
            bTracer.getSpans().values().forEach(span -> {
                tags.forEach((key, value) -> span.setTag(key, String.valueOf(value)));
            });
        }
    }

    private Map<String, Span> startSpan(String spanName, Map<String, Object> spanContextMap,
                                       Map<String, String> tags, String serviceName) {
        Map<String, Span> spanMap = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry spanContextEntry : spanContextMap.entrySet()) {
            Tracer tracer = tracers.get(spanContextEntry.getKey().toString());
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);

            for (Map.Entry<String, String> tag : tags.entrySet()) {
                spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
            }

            if (spanContextEntry.getValue() != null) {
                spanBuilder = spanBuilder.asChildOf((SpanContext) spanContextEntry.getValue());
            }

            Span span = spanBuilder.start();
            spanMap.put(spanContextEntry.getKey().toString(), span);
        }
        return spanMap;
    }

    private Map<String, Object> extract(Map<String, String> headers, String serviceName) {
        Map<String, Object> spanContext = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
            spanContext.put(tracerEntry.getKey(), tracerEntry.getValue()
                    .extract(Format.Builtin.HTTP_HEADERS, new RequestExtractor(headers)));
        }
        return spanContext;
    }

    private Map<String, String> inject(Map<String, ?> activeSpanMap, String serviceName) {
        Map<String, String> carrierMap = new HashMap<>();
        for (Map.Entry<String, ?> activeSpanEntry : activeSpanMap.entrySet()) {
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
            Tracer tracer = tracers.get(activeSpanEntry.getKey());
            if (tracer != null) {
                Span span = (Span) activeSpanEntry.getValue();
                if (span != null) {
                    tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,
                            new RequestInjector(TRACE_PREFIX, carrierMap));
                }
            }
        }
        return carrierMap;
    }

    private static Map<String, String> removeTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().substring(TRACE_PREFIX_LENGTH),
                        Map.Entry::getValue));
    }

}
