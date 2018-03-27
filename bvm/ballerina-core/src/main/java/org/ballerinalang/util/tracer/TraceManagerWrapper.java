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
import org.ballerinalang.util.tracer.factory.BTracerFactory;
import org.ballerinalang.util.tracer.factory.NoOpTracerFactory;
import org.ballerinalang.util.tracer.factory.TracerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.util.tracer.TraceConstants.TRACER_MANAGER_CLASS;
import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX;

/**
 * {@link TraceManagerWrapper} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.964.1
 */
public class TraceManagerWrapper {
    private static final TraceManagerWrapper instance = new TraceManagerWrapper();
    private TraceManager manager;
    private TracerFactory factory;
    private boolean enabled;

    private TraceManagerWrapper() {
        try {
            Class<?> tracerManagerClass = Class
                    .forName(TRACER_MANAGER_CLASS)
                    .asSubclass(TraceManager.class);
            manager = (TraceManager) tracerManagerClass.newInstance();
            enabled = manager.isEnabled();
            factory = (enabled) ? new BTracerFactory() : new NoOpTracerFactory();
        } catch (Exception e) {
            factory = new NoOpTracerFactory();
        }
    }

    public static TraceManagerWrapper getInstance() {
        return instance;
    }

    public static Tracer newTracer(WorkerExecutionContext ctx, boolean isClientCtx) {
        return getInstance().factory.getTracer(ctx, isClientCtx);
    }

    public boolean isTraceEnabled() {
        return enabled;
    }

    public void startSpan(WorkerExecutionContext ctx) {
        Tracer aBTracer = TraceUtil.getTracer(ctx);
        Tracer rBTracer = TraceUtil.getParentTracer(ctx.parent);

        String service = aBTracer.getConnectorName();
        String resource = aBTracer.getActionName();
        Long invocationId = Long.valueOf(aBTracer.getInvocationID());

        // get the parent spans' span context
        rBTracer = (rBTracer.getInvocationID() != null) ? rBTracer : aBTracer;
        Map<String, String> spanHeaders = rBTracer.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> String.valueOf(e.getValue())));

        Map<String, ?> spanContext = manager
                .extract(null, removeTracePrefix(spanHeaders), service);

        Map<String, ?> spanList = manager
                .startSpan(invocationId, resource, spanContext,
                        aBTracer.getTags(), true, service);

        Map<String, String> traceContextMap = manager
                .inject(spanList, null, service);

        aBTracer.getProperties().putAll(addTracePrefix(traceContextMap));
        aBTracer.setSpans(spanList);
    }

    public void finishSpan(Tracer tracer) {
        manager.finishSpan(new ArrayList<>(tracer.getSpans().values()));
    }

    public void finish(Tracer tracer) {
        manager.finishSpan(new ArrayList<>(tracer.getSpans().values()));
    }

    public void log(Tracer tracer, Map<String, Object> fields) {
        manager.log(new ArrayList<>(tracer.getSpans().values()), fields);
    }

    public void addTags(Tracer tracer, Map<String, String> tags) {
        manager.addTags(new ArrayList<>(tracer.getSpans().values()), tags);
    }

    private static Map<String, String> addTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> TRACE_PREFIX + e.getKey(),
                        Map.Entry::getValue)
                );
    }

    private static Map<String, String> removeTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().replaceFirst(TRACE_PREFIX, ""),
                        Map.Entry::getValue));
    }

}
