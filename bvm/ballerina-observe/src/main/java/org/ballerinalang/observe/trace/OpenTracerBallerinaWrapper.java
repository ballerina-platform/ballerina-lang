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
import org.ballerinalang.observe.trace.exception.UnknownSpanContextTypeException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class wraps opentracing apis and exposes native functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    private static final String NULL_CONTEXT = "null_context";
    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private SpanStore spanStore;

    public OpenTracerBallerinaWrapper() {
        OpenTracingConfig openTracingConfig = ConfigLoader.load();
        if (openTracingConfig != null) {
            tracerStore = new TracersStore(openTracingConfig);
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
        Map<String, SpanContext> spanContextMap = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers("default");
        boolean hasParent = true;
        for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
            spanContextMap.put(tracerEntry.getKey(), tracerEntry.getValue().extract(Format.Builtin.HTTP_HEADERS,
                    new RequestExtractor(spanHeaders.entrySet().iterator())));
            if (spanContextMap.get(tracerEntry.getKey()) == null) {
                hasParent = false;
            }
        }
        String spanId;
        if (hasParent) {
            spanId = UUID.randomUUID().toString();
            spanStore.addSpanContext(spanId, spanContextMap);
        } else {
            spanId = NULL_CONTEXT;
        }
        return spanId;
    }

    /**
     * Method to inject the span context of a span into a map carrier.
     *
     * @param spanId the span Id of the span to be injected
     * @return the map carrier holding the span context
     */
    public Map<String, String> inject(String spanId) {
        HashMap<String, String> carrierMap = new HashMap<>();
        Map<String, Span> activeSpanMap = spanStore.getSpan(spanId);
        for (Map.Entry<String, Span> activeSpanEntry : activeSpanMap.entrySet()) {
            Map<String, Tracer> tracers = tracerStore.getTracers("default");
            Tracer tracer = tracers.get(activeSpanEntry.getKey());
            if (tracer != null && activeSpanEntry.getValue() != null) {
                tracer.inject(activeSpanEntry.getValue().context(),
                        Format.Builtin.HTTP_HEADERS, new RequestInjector(carrierMap));
            }
        }
        return carrierMap;
    }

    public String startSpan(String invocationId, String serviceName, String spanName, Map<String, String> tags,
                            ReferenceType referenceType, String parentSpanId) {

        Map<String, Span> spanList = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        final Map<String, ?> parentSpanContext;
        if (NULL_CONTEXT.equals(parentSpanId) || parentSpanId == null) {
            parentSpanContext = new HashMap<>();
        } else {
            parentSpanContext = spanStore.getParent(parentSpanId);
        }
        tracers.forEach((tracerName, tracer) -> {
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
            }

            if (!parentSpanContext.isEmpty()) {
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
                    } else {
                        throw new UnknownSpanContextTypeException("Unknown span context field: " + parentSpan.getClass()
                                + "! Open tracing can span can be build only by using "
                                + SpanContext.class + " or " + Span.class);
                    }
                }
            }

            Span span = spanBuilder.start();
            tracer.scopeManager().activate(span, false);
            span.setBaggageItem(Constants.INVOCATION_ID_PROPERTY, String.valueOf(invocationId));
            spanList.put(tracerName, span);
        });

        String spanId = UUID.randomUUID().toString();
        spanStore.addSpan(spanId, spanList);
        return spanId;
    }

    public void finishSpan(String spanId) {
        Map<String, Span> spanMap = spanStore.closeSpan(spanId);
        if (spanMap != null) {
            spanMap.forEach((tracerName, span) -> span.finish());
        }

    }

    public void addTags(String spanId, String tagKey, String tagValue) {
        Map<String, Span> spanList = spanStore.getSpan(spanId);
        if (spanList != null) {
            spanList.forEach((tracerName, span) -> span.setTag(tagKey, tagValue));
        }
    }

    public void log(String spanId, Map<String, String> logs) {
        Map<String, Span> spanList = spanStore.getSpan(spanId);
        spanList.forEach((tracerName, span) -> span.log(logs));
    }
}
