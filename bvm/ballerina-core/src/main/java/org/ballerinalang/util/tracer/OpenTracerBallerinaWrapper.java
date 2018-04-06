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

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.ballerinalang.config.ConfigRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;
import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;

/**
 * This class wraps opentracing apis and exposes native functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    private static final String DEFAULT_TRACER = "default";
    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private SpanStore spanStore;
    private final boolean enabled;

    public OpenTracerBallerinaWrapper() {
        this.enabled =
                Boolean.parseBoolean(ConfigRegistry.getInstance().getConfiguration(CONFIG_TRACING_ENABLED));
        if (enabled) {
            Map<String, String> configurations = ConfigRegistry.getInstance().getConfigTable(CONFIG_TABLE_TRACING);
            tracerStore = new TracersStore(configurations);
            spanStore = new SpanStore();
        }
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    /**
     * Method to create an entry in span store by extracting a spanContext from a Map carrier.
     *
     * @param spanHeaders map of headers used to extract a spanContext
     * @return the map of span contexts for each tracer implementation
     */
    public Map<String, SpanContext> extract(Map<String, String> spanHeaders) {
        if (enabled) {
            Map<String, SpanContext> spanContextMap = new HashMap<>();
            Map<String, Tracer> tracers = tracerStore.getTracers(DEFAULT_TRACER);
            boolean hasParent = true;

            for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
                spanContextMap.put(tracerEntry.getKey(), tracerEntry.getValue().extract(Format.Builtin.HTTP_HEADERS,
                        new RequestExtractor(spanHeaders)));
                if (spanContextMap.get(tracerEntry.getKey()) == null) {
                    hasParent = false;
                }
            }

            if (hasParent) {
                return spanContextMap;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Method to inject the span context of a span into a map carrier.
     *
     * @param spanId the span Id of the span to be injected
     * @return the map carrier holding the span context
     */
    public Map<String, String> inject(String prefix, String spanId) {
        if (enabled) {
            Map<String, String> carrierMap = new HashMap<>();
            Map<String, Span> activeSpanMap = spanStore.getSpan(spanId);
            for (Map.Entry<String, Span> activeSpanEntry : activeSpanMap.entrySet()) {
                Map<String, Tracer> tracers = tracerStore.getTracers(DEFAULT_TRACER);
                Tracer tracer = tracers.get(activeSpanEntry.getKey());
                if (tracer != null && activeSpanEntry.getValue() != null) {
                    tracer.inject(activeSpanEntry.getValue().context(),
                            Format.Builtin.HTTP_HEADERS, new RequestInjector(prefix, carrierMap));
                }
            }
            return carrierMap;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Method to start a span using parent span id.
     *
     * @param serviceName   name of the service the span should belong to
     * @param spanName      name of the span
     * @param tags          key value paired tags to attach to the span
     * @param referenceType type of reference to any parent span
     * @param parentSpanId  id of the parent span
     * @return unique id of the created span
     */
    public String startSpan(String serviceName, String spanName, Map<String, String> tags, ReferenceType referenceType,
                            String parentSpanId) {
        if (enabled) {
            return startSpan(serviceName, spanName, tags, referenceType, spanStore.getSpanContext(parentSpanId));
        } else {
            return null;
        }
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param serviceName       name of the service the span should belong to
     * @param spanName          name of the span
     * @param tags              key value paired tags to attach to the span
     * @param referenceType     type of reference to any parent span
     * @param parentSpanContext map of the parent span context
     * @return unique id of the created span
     */
    public String startSpan(String serviceName, String spanName, Map<String, String> tags, ReferenceType referenceType,
                            Map<String, SpanContext> parentSpanContext) {
        if (enabled) {
            Map<String, Span> spanMap = new HashMap<>();
            Map<String, SpanContext> spanContextMap = new HashMap<>();
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);

            tracers.forEach((tracerName, tracer) -> {
                Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
                }

                if (parentSpanContext != null && !parentSpanContext.isEmpty()) {
                    spanBuilder = setParent(referenceType, parentSpanContext, spanBuilder, tracerName);
                }

                Span span = spanBuilder.start();
                spanMap.put(tracerName, span);
                spanContextMap.put(tracerName, span.context());
            });

            String spanId = UUID.randomUUID().toString();
            spanStore.addSpan(spanId, spanMap);
            spanStore.addSpanContext(spanId, spanContextMap);
            return spanId;
        } else {
            return null;
        }
    }

    private Tracer.SpanBuilder setParent(ReferenceType referenceType, Map<String, SpanContext> parentSpanContext,
                                         Tracer.SpanBuilder spanBuilder, String tracerName) {

        SpanContext parentSpan = parentSpanContext.get(tracerName);
        if (parentSpan != null) {
            if (ReferenceType.CHILDOF == referenceType) {
                spanBuilder = spanBuilder.asChildOf(parentSpan);
            } else if (ReferenceType.FOLLOWSFROM == referenceType) {
                spanBuilder.addReference(References.FOLLOWS_FROM, parentSpan);
            }
        }
        return spanBuilder;
    }

    /**
     * Method to mark a span as finished.
     *
     * @param spanId the id of the span to finish
     */
    public void finishSpan(String spanId) {
        if (enabled) {
            Map<String, Span> spanMap = spanStore.closeSpan(spanId);
            if (spanMap != null) {
                spanMap.forEach((tracerName, span) -> span.finish());
            }
        }
    }

    /**
     * Method to add tags to an existing span.
     *
     * @param spanId   the id of the span
     * @param tagKey   the key of the tag
     * @param tagValue the value of the tag
     */
    public void addTags(String spanId, String tagKey, String tagValue) {
        if (enabled) {
            Map<String, Span> spanList = spanStore.getSpan(spanId);
            if (spanList != null) {
                spanList.forEach((tracerName, span) -> span.setTag(tagKey, tagValue));
            }
        }
    }

    /**
     * Method to add logs to an existing span.
     *
     * @param spanId the id of the span
     * @param logs   the map (event, message) to be logged
     */
    public void log(String spanId, Map<String, String> logs) {
        if (enabled) {
            Map<String, Span> spanList = spanStore.getSpan(spanId);
            spanList.forEach((tracerName, span) -> span.log(logs));
        }
    }

    /**
     * Method to add baggage item to an existing span.
     *
     * @param spanId       the id of the span
     * @param baggageKey   the key of the baggage item
     * @param baggageValue the value of the baggage item
     */
    public void setBaggageItem(String spanId, String baggageKey, String baggageValue) {
        if (enabled) {
            Map<String, Span> spanList = spanStore.getSpan(spanId);
            spanList.forEach((tracerName, span) -> span.setBaggageItem(baggageKey, baggageValue));
        }
    }

    /**
     * Method to get a baggage value from an existing span.
     *
     * @param spanId     the id of the span
     * @param baggageKey the key of the baggage item. null of no baggage key or tracing disabled
     */
    public String getBaggageItem(String spanId, String baggageKey) {
        String baggageValue = null;
        if (enabled) {
            Map<String, Span> spanMap = spanStore.getSpan(spanId);
            for (Map.Entry<String, Span> spanEntry : spanMap.entrySet()) {
                baggageValue = spanEntry.getValue().getBaggageItem(baggageKey);
                if (baggageValue != null) {
                    break;
                }
            }
        }
        return baggageValue;
    }
}
