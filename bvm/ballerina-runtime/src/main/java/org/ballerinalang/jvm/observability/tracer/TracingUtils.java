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
package org.ballerinalang.jvm.observability.tracer;

import io.opentelemetry.api.trace.StatusCode;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.values.ErrorValue;

import java.util.Collections;
import java.util.Map;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_ERROR_VALUE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_SERVICE;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_SPAN;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.TAG_KEY_STR_ERROR_MESSAGE;

/**
 * Util class to hold tracing specific util methods.
 */
public final class TracingUtils {

    private TracingUtils() {
    }

    /**
     * Starts a span of an  {@link ObserverContext}.
     *
     * @param observerContext context that would hold the started span
     * @param isClient        true if the starting span is a client
     */
    public static void startObservation(ObserverContext observerContext, boolean isClient) {
        BSpan span;
        String serviceName = observerContext.getServiceName();
//        String operationName = observerContext.getOperationName();
        String operationName = observerContext.getResourceName() != null ? observerContext.getResourceName() :
                observerContext.getActionName() != null ? observerContext.getActionName() : UNKNOWN_SERVICE;
        if (observerContext.getParent() != null) {
            BSpan parentSpan = (BSpan) observerContext.getParent().getProperty(KEY_SPAN);
            span = BSpan.start(parentSpan, serviceName, operationName, isClient);
        } else {
            Map<String, String> httpHeaders =
                    (Map<String, String>) observerContext.getProperty(PROPERTY_TRACE_PROPERTIES);
            if (httpHeaders != null) {
                span = BSpan.start(httpHeaders, serviceName, operationName, isClient);
            } else {
                span = BSpan.start(serviceName, operationName, isClient);
            }
        }
        if (isClient) {
            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, span.extractContextAsHttpHeaders());
        }
        observerContext.addProperty(KEY_SPAN, span);
    }

    /**
     * Finishes a span in an {@link ObserverContext}.
     *
     * @param observerContext context that holds the span to be finished
     */
    public static void stopObservation(ObserverContext observerContext) {
        BSpan span = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (span != null) {
            // Adding error message to Trace Span
            ErrorValue bError = (ErrorValue) observerContext.getProperty(PROPERTY_ERROR_VALUE);
            if (bError != null) {
                span.addTag(TAG_KEY_STR_ERROR_MESSAGE, bError.getPrintableStackTrace());
                span.setStatus(StatusCode.ERROR);
            } else {
                span.setStatus(StatusCode.OK);
            }

            // Adding specific error code to Trace Span
            Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
            if (statusCode != null && statusCode >= 100) {
                span.addTags(Collections.singletonMap(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(statusCode)));
            }

            Map<String, String> traceTags = observerContext.getTags();
            span.addTags(traceTags);
            span.finishSpan();
        }
    }
}
