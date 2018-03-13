/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.ballerinalang.observe.trace;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.ballerinalang.observe.trace.config.ConfigLoader;
import org.ballerinalang.observe.trace.config.OpenTracingConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class wraps opentracing apis and exposes native functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    static final String ROOT_CONTEXT = "root_context";
    private static final String DEFAULT_TRACER = "default";
    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private SpanStore spanStore;
    private boolean enabled = false;

    public OpenTracerBallerinaWrapper() {
        OpenTracingConfig openTracingConfig = ConfigLoader.load();
        if (openTracingConfig != null) {
            tracerStore = new TracersStore(openTracingConfig);
            enabled = true;
        }
        spanStore = new SpanStore();
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    /**
     * Method to create an entry in span store by extracting a spanContext from a Map carrier.
     *
     * @param spanHeaders map of headers used to extract a spanContext
     * @return the Id of the span context
     */
    public String extract(Map<String, String> spanHeaders) {
        String parentSpanId = null;
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
                parentSpanId = UUID.randomUUID().toString();
                spanStore.addSpanContext(parentSpanId, spanContextMap);
            } else {
                parentSpanId = ROOT_CONTEXT;
            }
        }
        return parentSpanId;
    }

    /**
     * Method to inject the span context of a span into a map carrier.
     *
     * @param spanId the span Id of the span to be injected
     * @return the map carrier holding the span context
     */
    public Map<String, String> inject(String spanId) {
        Map<String, String> carrierMap = new HashMap<>();
        Map<String, Span> activeSpanMap = spanStore.getSpan(spanId);
        if (enabled) {
            for (Map.Entry<String, Span> activeSpanEntry : activeSpanMap.entrySet()) {
                Map<String, Tracer> tracers = tracerStore.getTracers(DEFAULT_TRACER);
                Tracer tracer = tracers.get(activeSpanEntry.getKey());
                if (tracer != null && activeSpanEntry.getValue() != null) {
                    tracer.inject(activeSpanEntry.getValue().context(),
                            Format.Builtin.HTTP_HEADERS, new RequestInjector(carrierMap));
                }
            }
        }
        return carrierMap;
    }

    /**
     * Method to start a span.
     *
     * @param invocationId  unique id for each invocation
     * @param serviceName   name of the service the span should belong to
     * @param spanName      name of the span
     * @param tags          key value paired tags to attach to the span
     * @param referenceType type of reference to any parent span
     * @param parentSpanId  id of the parent span
     * @return unique id of the created span
     */
    public String startSpan(String invocationId, String serviceName, String spanName, Map<String, String> tags,
                            ReferenceType referenceType, String parentSpanId) {
        if (enabled) {
            Map<String, Span> spanMap = new HashMap<>();
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);

            final Map parentSpanContext;
            if (ROOT_CONTEXT.equals(parentSpanId) || parentSpanId == null) {
                parentSpanContext = new HashMap<>();
            } else {
                parentSpanContext = spanStore.getParent(parentSpanId);
            }

            tracers.forEach((tracerName, tracer) -> {
                Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
                }

                if (parentSpanContext != null && !parentSpanContext.isEmpty()) {
                    spanBuilder = setParent(referenceType, parentSpanContext, spanBuilder, tracerName);
                }

                Span span = spanBuilder.start();
                tracer.scopeManager().activate(span, false);
                span.setBaggageItem(Constants.INVOCATION_ID_PROPERTY, String.valueOf(invocationId));
                spanMap.put(tracerName, span);
            });

            String spanId = UUID.randomUUID().toString();
            spanStore.addSpan(spanId, spanMap);
            return spanId;
        } else {
            return null;
        }
    }

    private Tracer.SpanBuilder setParent(ReferenceType referenceType, Map parentSpanContext,
                                         Tracer.SpanBuilder spanBuilder, String tracerName) {

        Object parentSpan = parentSpanContext.get(tracerName);
        if (parentSpan != null) {
            if (parentSpan instanceof SpanContext) {
                if (ReferenceType.CHILDOF == referenceType) {
                    spanBuilder = spanBuilder.asChildOf((SpanContext) parentSpan);
                } else if (ReferenceType.FOLLOWSFROM == referenceType) {
                    spanBuilder.addReference(References.FOLLOWS_FROM, (SpanContext) parentSpan);
                }
            } else if (parentSpan instanceof Span) {
                if (ReferenceType.CHILDOF == referenceType) {
                    spanBuilder = spanBuilder.asChildOf((((Span) parentSpan).context()));
                } else if (ReferenceType.FOLLOWSFROM == referenceType) {
                    spanBuilder.addReference(References.FOLLOWS_FROM, (((Span) parentSpan).context()));
                }
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
     * @param baggageKey the key of the baggage item
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
