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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.observability.tracer.BSpan;
import io.ballerina.runtime.observability.tracer.TracersStore;
import io.ballerina.runtime.observability.tracer.TracingUtils;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.ballerina.runtime.observability.ObservabilityConstants.DEFAULT_SERVICE_NAME;

/**
 * This class wraps opentracing apis and exposes extern functions to use within ballerina.
 */
public class OpenTracerBallerinaWrapper {
    private static final Logger log = LoggerFactory.getLogger(OpenTracerBallerinaWrapper.class);

    private static final OpenTracerBallerinaWrapper instance = new OpenTracerBallerinaWrapper();
    private final TracersStore tracerStore;
    private final boolean enabled;
    private final Map<Long, ObserverContext> observerContextMap = new HashMap<>();
    private final AtomicLong spanIdCounter = new AtomicLong();

    private static final int SYSTEM_TRACE_INDICATOR = -1;
    static final int ROOT_SPAN_INDICATOR = -2;

    private OpenTracerBallerinaWrapper() {
        enabled = ObserveUtils.isTracingEnabled();
        tracerStore = TracersStore.getInstance();
    }

    public static OpenTracerBallerinaWrapper getInstance() {
        return instance;
    }

    private long startSpan(ObserverContext observerContext, boolean isClient, String spanName) {
        observerContext.setOperationName(spanName);
        TracingUtils.startObservation(observerContext, isClient);
        long spanId = this.spanIdCounter.getAndIncrement();
        observerContextMap.put(spanId, observerContext);
        return spanId;
    }

    /**
     * Method to start a span using parent span context.
     *
     * @param env          native context
     * @param spanName     name of the span
     * @param tags         key value paired tags to attach to the span
     * @param parentSpanId id of parent span
     * @return unique id of the created span
     */
    public long startSpan(Environment env, String spanName, Map<String, String> tags, long parentSpanId) {
        if (!enabled) {
            return -1;
        }

        ObserverContext observerContext = new ObserverContext();
        ObserverContext prevObserverContext = ObserveUtils.getObserverContextOfCurrentFrame(env);
        String serviceName;
        if (prevObserverContext != null) {
            serviceName = prevObserverContext.getServiceName();
            observerContext.setEntrypointFunctionModule(prevObserverContext.getEntrypointFunctionModule());
            observerContext.setEntrypointFunctionPosition(prevObserverContext.getEntrypointFunctionPosition());
        } else {
            serviceName = DEFAULT_SERVICE_NAME;
        }
        observerContext.setServiceName(serviceName);

        Tracer tracer = tracerStore.getTracer(serviceName);
        if (tracer == null) {
            return -1;
        }
        tags.forEach((observerContext::addTag));

        if (parentSpanId == SYSTEM_TRACE_INDICATOR) {
            observerContext.setSystemSpan(true);
            if (prevObserverContext != null) {
                observerContext.setParent(prevObserverContext);
            }
            ObserveUtils.setObserverContextToCurrentFrame(env, observerContext);
            return startSpan(observerContext, true, spanName);
        } else if (parentSpanId != ROOT_SPAN_INDICATOR) {
            ObserverContext parentOContext = observerContextMap.get(parentSpanId);
            if (parentOContext == null) {
                return -1;
            }
            observerContext.setParent(parentOContext);
            return startSpan(observerContext, true, spanName);
        }

        return startSpan(observerContext, true, spanName);
    }

    /**
     * Method to mark a span as finished.
     *
     * @param env current environment
     * @param spanId id of the Span
     * @return boolean to indicate if span was finished
     */
    public boolean finishSpan(Environment env, long spanId) {
        if (!enabled) {
            return false;
        }
        ObserverContext observerContext = observerContextMap.get(spanId);
        if (observerContext != null) {
            if (observerContext.isSystemSpan()) {
                ObserveUtils.setObserverContextToCurrentFrame(env, observerContext.getParent());
            }
            TracingUtils.stopObservation(observerContext);
            observerContext.setFinished();
            observerContextMap.remove(spanId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to add tags to an existing span.
     *
     * @param env      current environment
     * @param tagKey   the key of the tag
     * @param tagValue the value of the tag
     * @param spanId   id of the Span
     * @return Object to indicate if tag was added to the span
     */
    public Object addTag(Environment env, String tagKey, String tagValue, long spanId) {

        if (!enabled) {
            return null;
        }

        final BSpan span;
        if (spanId == -1) {
            ObserverContext observer = ObserveUtils.getObserverContextOfCurrentFrame(env);
            if (observer == null) {
                return ErrorCreator.createError(
                        StringUtils.fromString(
                                ("Span already finished. Can not add tag {" + tagKey + ":" + tagValue + "}")));
            }
            span = observer.getSpan();
        } else {
            ObserverContext observerContext = observerContextMap.get(spanId);
            if (observerContext == null) {
                String errorMsg =
                        "Could not find the trace for given span id. Can not add tag {" + tagKey + ":" + tagValue +
                                "}";
                log.info(errorMsg);
                return ErrorCreator.createError(StringUtils.fromString(errorMsg));
            }
            span = observerContext.getSpan();
        }

        span.addTag(tagKey, tagValue);
        return null;
    }
}
