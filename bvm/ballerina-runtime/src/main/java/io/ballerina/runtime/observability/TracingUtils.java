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
package io.ballerina.runtime.observability;

import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.observability.metrics.Tag;
import io.ballerina.runtime.observability.tracer.BSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_BSTRUCT_ERROR;
import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_ERROR_MESSAGE;
import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ENTRYPOINT_FUNCTION_MODULE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ENTRYPOINT_FUNCTION_POSITION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ERROR;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_TRUE_VALUE;
import static io.ballerina.runtime.observability.tracer.TraceConstants.KEY_SPAN;
import static io.ballerina.runtime.observability.tracer.TraceConstants.LOG_ERROR_KIND_EXCEPTION;
import static io.ballerina.runtime.observability.tracer.TraceConstants.LOG_EVENT_TYPE_ERROR;
import static io.ballerina.runtime.observability.tracer.TraceConstants.LOG_KEY_ERROR_KIND;
import static io.ballerina.runtime.observability.tracer.TraceConstants.LOG_KEY_EVENT_TYPE;
import static io.ballerina.runtime.observability.tracer.TraceConstants.LOG_KEY_MESSAGE;
import static io.ballerina.runtime.observability.tracer.TraceConstants.TAG_KEY_HTTP_STATUS_CODE;

/**
 * Util class to hold tracing specific util methods.
 */
public class TracingUtils {

    private TracingUtils() {
    }

    /**
     * Starts a span of an  {@link ObserverContext}.
     *
     * @param observerContext context that would hold the started span
     * @param isClient        true if the starting span is a client
     */
    public static void startObservation(ObserverContext observerContext, boolean isClient) {
        BSpan span = new BSpan(observerContext, isClient);
        span.setServiceName(observerContext.getServiceName());

        span.setOperationName(observerContext.getOperationName());
        if (isClient) {
            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, span.getProperties());
        } else {
            Map<String, String> httpHeaders =
                    (Map<String, String>) observerContext.getProperty(PROPERTY_TRACE_PROPERTIES);

            if (httpHeaders != null) {
                httpHeaders.entrySet()
                        .forEach(e -> span.addProperty(e.getKey(), e.getValue()));
            }
        }

        observerContext.addProperty(KEY_SPAN, span);
        span.startSpan();
    }

    /**
     * Finishes a span in an {@link ObserverContext}.
     *
     * @param observerContext context that holds the span to be finished
     */
    public static void stopObservation(ObserverContext observerContext) {
        BSpan span = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (span != null) {
            Tag errorTag = observerContext.getTag(TAG_KEY_ERROR);
            if (errorTag != null && TAG_TRUE_VALUE.equals(errorTag.getValue())) {
                StringBuilder errorMessageBuilder = new StringBuilder();
                String errorMessage = (String) observerContext.getProperty(PROPERTY_ERROR_MESSAGE);
                if (errorMessage != null) {
                    errorMessageBuilder.append(errorMessage);
                }

                ErrorValue bError = (ErrorValue) observerContext.getProperty(PROPERTY_BSTRUCT_ERROR);
                if (bError != null) {
                    if (errorMessage != null) {
                        errorMessageBuilder.append('\n');
                    }
                    errorMessageBuilder.append(bError.getPrintableStackTrace());
                }
                Map<String, Object> logProps = new HashMap<>();
                logProps.put(LOG_KEY_ERROR_KIND, LOG_ERROR_KIND_EXCEPTION);
                logProps.put(LOG_KEY_EVENT_TYPE, LOG_EVENT_TYPE_ERROR);
                logProps.put(LOG_KEY_MESSAGE, errorMessageBuilder.toString());
                span.logError(logProps);
            }
            Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
            if (statusCode != null && statusCode >= 100) {
                span.addTags(Collections.singletonMap(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(statusCode)));
            }
            Map<String, String> traceTags = observerContext.getAllTags()
                    .stream()
                    .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
            if (observerContext.getEntrypointFunctionModule() != null) {
                traceTags.put(TAG_KEY_ENTRYPOINT_FUNCTION_MODULE, observerContext.getEntrypointFunctionModule());
            }
            if (observerContext.getEntrypointFunctionPosition() != null) {
                traceTags.put(TAG_KEY_ENTRYPOINT_FUNCTION_POSITION, observerContext.getEntrypointFunctionPosition());
            }
            span.addTags(traceTags);
            span.finishSpan();
        }
    }
}
