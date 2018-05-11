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
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.tracer.TracersStore;

import java.util.Map;

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
        enabled = ConfigRegistry.getInstance().getAsBoolean(CONFIG_TRACING_ENABLED);
        if (enabled) {
            tracerStore = TracersStore.getInstance();
            spanStore = new SpanStore();
        }
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param serviceName  name of the service the span should belong to
     * @param spanName     name of the span
     * @param tags         key value paired tags to attach to the span
     * @param invocationId
     * @return unique id of the created span
     */
    public String startSpan(String serviceName, String spanName, Map<String, String> tags, String invocationId) {
        if (enabled) {
            BSpan bSpan = null;
            Tracer tracer = tracerStore.getTracer(serviceName);
            if (tracer != null) {
                Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
                }

                BSpan activeSpan = spanStore.getActiveSpan(invocationId);
                if (activeSpan != null) {
                    spanBuilder.asChildOf(activeSpan.getSpan());
                }

                Span span = spanBuilder.start();
                bSpan = new BSpan(invocationId, span, activeSpan);
                spanStore.addSpan(bSpan);
            }
            if (bSpan != null) {
                return bSpan.getSpanId();
            }
        }
        return null;
    }

    /**
     * Method to mark a span as finished.
     *
     * @param spanId the id of the span to finish
     */
    public void finishSpan(String invocationId, String spanId) {
        if (enabled) {
            BSpan bSpan = spanStore.finishAndRemoveSpan(invocationId, spanId);
            if (bSpan != null) {
                bSpan.getSpan().finish();
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
            BSpan bSpan = spanStore.getSpan(spanId);
            if (bSpan != null) {
                bSpan.getSpan().setTag(tagKey, tagValue);
            }
        }
    }
}
