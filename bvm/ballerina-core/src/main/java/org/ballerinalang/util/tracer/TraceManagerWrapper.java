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

import org.ballerinalang.bre.bvm.WorkerExecutionContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.util.tracer.TraceConstants.TRACER_MANAGER_CLASS;
import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX_LENGTH;

/**
 * {@link TraceManagerWrapper} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.964.1
 */
public class TraceManagerWrapper {
    private static final TraceManagerWrapper instance = new TraceManagerWrapper();
    private TraceManager manager;
    private boolean enabled;

    private TraceManagerWrapper() {
        try {
            Class<?> tracerManagerClass = Class
                    .forName(TRACER_MANAGER_CLASS)
                    .asSubclass(TraceManager.class);
            manager = (TraceManager) tracerManagerClass.newInstance();
            enabled = manager.isEnabled();
        } catch (Throwable ignored) {
            enabled = false;
        }
    }

    public static TraceManagerWrapper getInstance() {
        return instance;
    }

    public boolean isTraceEnabled() {
        return enabled;
    }

    public void startSpan(WorkerExecutionContext ctx) {
        if (enabled) {
            BTracer activeTracer = TraceUtil.getTracer(ctx);
            if (activeTracer != null) {
                BTracer parentTracer = TraceUtil.getParentTracer(ctx.parent);

                String service = activeTracer.getConnectorName();
                String resource = activeTracer.getActionName();

                // get the parent spans' span context
                parentTracer = (parentTracer != null) ? parentTracer : activeTracer;
                Map<String, String> spanHeaders = parentTracer.getProperties().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

                Map<String, ?> spanContext = manager.extract(removeTracePrefix(spanHeaders), service);
                Map<String, ?> spanList = manager.startSpan(resource, spanContext, activeTracer.getTags(), service);
                Map<String, String> traceContextMap = manager.inject(spanList, service);

                activeTracer.getProperties().putAll(traceContextMap);
                activeTracer.setSpans(spanList);
            }
        }
    }

    public void finishSpan(BTracer tracer) {
        if (enabled) {
            manager.finishSpan(new ArrayList<>(tracer.getSpans().values()));
        }
    }

    public void log(BTracer tracer, Map<String, Object> fields) {
        if (enabled) {
            manager.log(new ArrayList<>(tracer.getSpans().values()), fields);
        }
    }

    public void addTags(BTracer tracer, Map<String, String> tags) {
        if (enabled) {
            manager.addTags(new ArrayList<>(tracer.getSpans().values()), tags);
        }
    }

    private static Map<String, String> removeTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().substring(TRACE_PREFIX_LENGTH),
                        Map.Entry::getValue));
    }

}
