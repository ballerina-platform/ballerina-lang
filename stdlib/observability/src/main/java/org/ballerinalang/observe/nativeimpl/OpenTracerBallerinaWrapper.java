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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.observability.TracingUtils;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.tracer.TracersStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.UNKNOWN_SERVICE;

/**
 * This class wraps opentracing apis and exposes extern functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {

    private static OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private TracersStore tracerStore;
    private final boolean enabled;
    private Map<Integer, ObserverContext> observerContextList = new HashMap<>();
    private AtomicInteger spanId = new AtomicInteger();
    private static final int SYSTEM_TRACE_INDICATOR = -1;

    static final int ROOT_SPAN_INDICATOR = -2;


    private OpenTracerBallerinaWrapper() {
        enabled = ConfigRegistry.getInstance().getAsBoolean(CONFIG_TRACING_ENABLED);
        tracerStore = TracersStore.getInstance();
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param spanName     name of the span
     * @param tags         key value paired tags to attach to the span
     * @param parentSpanId id of parent span
     * @param context      native context
     * @return unique id of the created span
     */
    public int startSpan(String spanName, Map<String, String> tags, int parentSpanId, Context context) {
        if (!enabled) {
            return -1;
        }

        WorkerExecutionContext workerExecutionContext = context.getParentWorkerExecutionContext();
        ServiceInfo serviceInfo = BLangVMUtils.getServiceInfo(workerExecutionContext);
        String serviceName;
        if (serviceInfo != null) {
            serviceName = ObservabilityUtils.getFullServiceName(serviceInfo);
        } else {
            serviceName = UNKNOWN_SERVICE;
        }
        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer == null) {
            return -1;
        }

        ObserverContext observerContext = new ObserverContext();
        observerContext.setServiceName(serviceName);
        observerContext.setResourceName(spanName);
        tags.forEach((observerContext::addTag));

        if (parentSpanId == SYSTEM_TRACE_INDICATOR) {
            ObservabilityUtils.getParentContext(context).ifPresent(observerContext::setParent);
            ObservabilityUtils.setObserverContextToWorkerExecutionContext(workerExecutionContext, observerContext);
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

    private int startSpan(ObserverContext observerContext, boolean isClient, String spanName) {
        observerContext.setActionName(spanName);
        TracingUtils.startObservation(observerContext, isClient);
        int spanId = this.spanId.getAndIncrement();
        observerContextList.put(spanId, observerContext);
        return spanId;
    }

    /**
     * Method to mark a span as finished.
     *
     * @param spanId id of the Span
     * @return boolean to indicate if span was finished
     */
    public boolean finishSpan(int spanId) {
        if (!enabled) {
            return false;
        }
        ObserverContext observerContext = observerContextList.get(spanId);
        if (observerContext != null) {
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
     * @param tagKey   the key of the tag
     * @param tagValue the value of the tag
     * @param spanId   id of the Span
     * @param context  native context
     * @return boolean to indicate if tag was added to the span
     */
    public boolean addTag(String tagKey, String tagValue, int spanId, Context context) {
        if (!enabled) {
            return false;
        }
        ObserverContext observerContext = observerContextList.get(spanId);
        if (spanId == -1) {
            Optional<ObserverContext> observer = ObservabilityUtils.getParentContext(context);
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
