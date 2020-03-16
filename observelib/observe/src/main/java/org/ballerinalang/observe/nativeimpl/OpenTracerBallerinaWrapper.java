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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import io.opentracing.Tracer;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.TracingUtils;
import org.ballerinalang.jvm.observability.tracer.TracersStore;
import org.ballerinalang.jvm.scheduling.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_RESOURCE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_SERVICE;

/**
 * This class wraps opentracing apis and exposes extern functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private final boolean enabled;
    private Map<Long, ObserverContext> observerContextList = new HashMap<>();
    private AtomicLong spanId = new AtomicLong();
    private static final int SYSTEM_TRACE_INDICATOR = -1;

    static final int ROOT_SPAN_INDICATOR = -2;


    private OpenTracerBallerinaWrapper() {
        enabled = ConfigRegistry.getInstance().getAsBoolean(CONFIG_TRACING_ENABLED);
        tracerStore = TracersStore.getInstance();
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    private long startSpan(ObserverContext observerContext, boolean isClient, String spanName) {
        observerContext.setActionName(spanName);
        TracingUtils.startObservation(observerContext, isClient);
        long spanId = this.spanId.getAndIncrement();
        observerContextList.put(spanId, observerContext);
        return spanId;
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param serviceName  name of the service to which the span is attached to
     * @param spanName     name of the span
     * @param tags         key value paired tags to attach to the span
     * @param parentSpanId id of parent span
     * @param strand       native context
     * @return unique id of the created span
     */
    public long startSpan(String serviceName, String spanName, Map<String, String> tags, long parentSpanId,
                          Strand strand) {
        if (!enabled) {
            return -1;
        }

        Optional<ObserverContext> observerContextOfCurrentFrame = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        if (serviceName == null && observerContextOfCurrentFrame.isPresent()) {
            serviceName = observerContextOfCurrentFrame.get().getServiceName();
        }
        if (serviceName == null) {
            serviceName = UNKNOWN_SERVICE;
        }

        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer == null) {
            return -1;
        }

        ObserverContext observerContext = new ObserverContext();
        observerContext.setServiceName(serviceName);
        observerContext.setResourceName(observerContextOfCurrentFrame.isPresent()
                ? observerContextOfCurrentFrame.get().getResourceName()
                : UNKNOWN_RESOURCE);
        tags.forEach((observerContext::addTag));

        if (parentSpanId == SYSTEM_TRACE_INDICATOR) {
            observerContext.setSystemSpan(true);
            observerContextOfCurrentFrame.ifPresent(observerContext::setParent);
            ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext);
            return startSpan(observerContext, true, spanName);
        } else if (parentSpanId != ROOT_SPAN_INDICATOR) {
            ObserverContext parentOContext = observerContextList.get(parentSpanId);
            if (parentOContext == null) {
                return -1;
            }
            observerContext.setParent(parentOContext);
            return startSpan(observerContext, true, spanName);
        }

        return startSpan(observerContext, false, spanName);
    }

    /**
     * Method to mark a span as finished.
     *
     *
     * @param strand current context
     * @param spanId id of the Span
     * @return boolean to indicate if span was finished
     */
    public boolean finishSpan(Strand strand, long spanId) {
        if (!enabled) {
            return false;
        }
        ObserverContext observerContext = observerContextList.get(spanId);
        if (observerContext != null) {
            if (observerContext.isSystemSpan()) {
                ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext.getParent());
            }
            TracingUtils.stopObservation(observerContext);
            observerContext.setFinished();
            observerContextList.remove(spanId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to add tags to an existing span.
     *
     * @param tagKey the key of the tag
     * @param tagValue the value of the tag
     * @param spanId id of the Span
     * @param strand current strand
     * @return boolean to indicate if tag was added to the span
     */
    public boolean addTag(String tagKey, String tagValue, long spanId, Strand strand) {
        if (!enabled) {
            return false;
        }
        ObserverContext observerContext = observerContextList.get(spanId);
        if (spanId == -1) {
            Optional<ObserverContext> observer = ObserveUtils.getObserverContextOfCurrentFrame(strand);
            if (observer.isPresent()) {
                observer.get().addTag(tagKey, tagValue);
                return true;
            }
        }
        if (observerContext != null) {
            observerContext.addTag(tagKey, tagValue);
            return true;
        } else {
            return false;
        }
    }
}
