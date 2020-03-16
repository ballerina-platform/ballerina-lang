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
package org.ballerinalang.jvm.observability;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.tracer.BSpan;
import org.ballerinalang.jvm.values.ErrorValue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_BSTRUCT_ERROR;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_ERROR;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_ERROR_MESSAGE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_SPAN;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.LOG_ERROR_KIND_EXCEPTION;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.LOG_EVENT_TYPE_ERROR;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.LOG_KEY_ERROR_KIND;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.LOG_KEY_EVENT_TYPE;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.LOG_KEY_MESSAGE;

/**
 * Util class to hold tracing specific util methods.
 */
public class TracingUtils {

    private static final String SEPARATOR = ":";

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
        span.setConnectorName(observerContext.getServiceName() != null ?
                observerContext.getServiceName() : ObservabilityConstants.UNKNOWN_SERVICE);

        if (isClient) {
            span.setActionName(StringUtils.isNotEmpty(observerContext.getConnectorName()) ?
                    observerContext.getConnectorName() + SEPARATOR + observerContext.getActionName()
                    : observerContext.getActionName());
            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, span.getProperties());
        } else {
            span.setActionName(observerContext.getResourceName());
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
            Boolean error = (Boolean) observerContext.getProperty(PROPERTY_ERROR);
            if (error != null && error) {
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
            span.addTags(observerContext.getAllTags()
                    .stream()
                    .collect(Collectors.toMap(Tag::getKey, Tag::getValue)));
            span.finishSpan();
        }
    }
}
