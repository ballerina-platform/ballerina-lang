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

package org.ballerinalang.util.tracer;

import org.ballerinalang.bre.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * BallerinaTracerManager to manage tracing.
 */
public class BallerinaTracerManager {
    private static final Random RANDOM = new Random();
    private static BallerinaTracerManager instance = new BallerinaTracerManager();
    private TracerManager manager;
    private boolean enabled;

    private BallerinaTracerManager() {
        try {
            Class<?> tracerManagerClass = Class.forName(TraceConstants.TRACER_MANAGER_CLASS)
                    .asSubclass(TracerManager.class);
            manager = (TracerManager) tracerManagerClass.newInstance();
            enabled = true;
        } catch (Throwable t) {
            enabled = false;
        }
    }

    public static BallerinaTracerManager getInstance() {
        return instance;
    }

    public String buildSpan(Context context) {
        if (enabled) {
            if (context.traceContext.isEmpty()) {
                context.traceContext.put(TraceConstants.TRACE_PREFIX + TraceConstants.TRACE_PROPERTY_INVOCATION_ID,
                        String.valueOf(RANDOM.nextLong()));
            }

            Long invocationId = Long.valueOf(context.traceContext
                    .get(TraceConstants.TRACE_PREFIX + TraceConstants.TRACE_PROPERTY_INVOCATION_ID));
            String serviceId = (String) context.getProperty(TraceConstants.TRACE_PROPERTY_SERVICE);
            serviceId = (serviceId == null || serviceId.isEmpty()) ? "ballerina" : serviceId;
            String resourceId = (String) context.getProperty(TraceConstants.TRACE_PROPERTY_RESOURCE);
            resourceId = (resourceId == null || resourceId.isEmpty()) ? "default" : resourceId;

            Map<String, String> tags = new HashMap<>();
            tags.put("span.kind", "server-receive");
            tags.put("invocationId", String.valueOf(invocationId));
            tags.put("serviceId", serviceId);
            tags.put("resourceId", resourceId);

            Map<String, String> spanHeaders = context.traceContext.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
            Map<String, Object> spanContext = manager.extract(null, removeTracePrefix(spanHeaders), serviceId);
            List<Object> spanList = manager.buildSpan(invocationId, resourceId, spanContext, tags, true, serviceId);
            Map<String, String> tContext = manager.inject(manager.getActiveSpanMap(serviceId), null, serviceId);
            context.traceContext.putAll(addTracePrefix(tContext));

            if (spanContext != null && !spanContext.isEmpty()) {
                return SpanHolder.getInstance().onBuildSpan(String.valueOf(invocationId), spanList, spanContext);
            } else {
                return SpanHolder.getInstance().onBuildSpan(String.valueOf(invocationId), spanList, null);
            }
        }
        return null;
    }

    public void finishSpan(Context context, String spanId) {
        if (enabled) {
            Long invocationId = Long.valueOf(context.traceContext
                    .get(TraceConstants.TRACE_PREFIX + TraceConstants.TRACE_PROPERTY_INVOCATION_ID));
            String serviceId = (String) context.getProperty(TraceConstants.TRACE_PROPERTY_SERVICE);
            serviceId = (serviceId == null || serviceId.isEmpty()) ? "ballerina" : serviceId;

            SpanHolder.SpanReference spanRef = SpanHolder.getInstance()
                    .onFinishSpan(String.valueOf(invocationId), spanId);
            manager.finishSpan(spanRef.getSpans(), spanRef.getParent(), serviceId);
        }
    }

    private Map<String, String> addTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> TraceConstants.TRACE_PREFIX + e.getKey(), Map.Entry::getValue));
    }

    private Map<String, String> removeTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey()
                        .replaceFirst(TraceConstants.TRACE_PREFIX, ""), Map.Entry::getValue));
    }

}
