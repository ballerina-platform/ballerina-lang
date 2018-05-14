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

package org.ballerinalang.nativeimpl.observe.tracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.ballerinalang.bre.Context;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.tracer.TraceConstants;
import org.ballerinalang.util.tracer.TracersStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.tracer.TraceConstants.KEY_USER_SPAN;

/**
 * This class wraps opentracing apis and exposes native functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private final boolean enabled;

    public OpenTracerBallerinaWrapper() {
        enabled = ConfigRegistry.getInstance().getAsBoolean(CONFIG_TRACING_ENABLED);
        tracerStore = TracersStore.getInstance();
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param serviceName name of the service the span should belong to
     * @param spanName    name of the span
     * @param tags        key value paired tags to attach to the span
     * @param context     native context
     * @return unique id of the created span
     */
    public String startSpan(String serviceName, String spanName, Map<String, String> tags, Context context) {
        if (!enabled) {
            return null;
        }

        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer == null) {
            return null;
        }

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            spanBuilder.withTag(tag.getKey(), tag.getValue());
        }
        AtomicReference<SpanStore> spanStore = new AtomicReference<>(
                (SpanStore) context.getParentWorkerExecutionContext().localProps.get(KEY_USER_SPAN));
        BSpan parentSpan = null;
        if (spanStore.get() != null) {
            parentSpan = spanStore.get().getActiveBSpan();
            if (parentSpan != null) {
                spanBuilder.asChildOf(parentSpan.getSpan());
            }
        } else {
            Optional<ObserverContext> optionalObserverContext = ObservabilityUtils.getParentContext(context);
            optionalObserverContext.ifPresent(observerContext -> {
                spanStore.set(new SpanStore());
                org.ballerinalang.util.tracer.BSpan bSpan =
                        (org.ballerinalang.util.tracer.BSpan) observerContext.getProperty(TraceConstants.KEY_SPAN);
                if (bSpan != null) {
                    spanBuilder.asChildOf(bSpan.getSpan());
                }
            });
        }
        Span span = spanBuilder.start();
        BSpan bSpan = new BSpan(span, parentSpan);
        spanStore.get().addSpan(bSpan);
        context.getParentWorkerExecutionContext().localProps.put(KEY_USER_SPAN, spanStore.get());
        return bSpan.getSpanId();

    }

    /**
     * Method to mark a span as finished.
     *
     * @param spanId  the id of the span to finish
     * @param context native context
     */
    public void finishSpan(String spanId, Context context) {
        if (enabled) {
            SpanStore spanStore =
                    (SpanStore) context.getParentWorkerExecutionContext().localProps.get(KEY_USER_SPAN);
            BSpan bSpan = spanStore.finishAndRemoveSpan(spanId);
            bSpan.getSpan().finish();
        }
    }

    /**
     * Method to add tags to an existing span.
     *
     * @param spanId   the id of the span
     * @param tagKey   the key of the tag
     * @param tagValue the value of the tag
     * @param context  native context
     */
    public void addTags(String spanId, String tagKey, String tagValue, Context context) {
        if (enabled) {
            SpanStore spanStore =
                    (SpanStore) context.getParentWorkerExecutionContext().localProps.get(KEY_USER_SPAN);
            spanStore.getSpan(spanId).getSpan().setTag(tagKey, tagValue);
        }
    }
}
