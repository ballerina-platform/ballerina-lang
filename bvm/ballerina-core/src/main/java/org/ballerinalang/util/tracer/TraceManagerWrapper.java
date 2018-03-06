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
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.util.tracer.TraceConstant.STR_NULL;
import static org.ballerinalang.util.tracer.TraceConstant.TRACER_MANAGER_CLASS;
import static org.ballerinalang.util.tracer.TraceConstant.TRACE_PREFIX;

/**
 * {@link TraceManagerWrapper} loads {@link TraceManager} implementation
 * and wraps it's functionality.
 *
 * @since 0.963.1
 */
public class TraceManagerWrapper {
    private static TraceManagerWrapper instance = new TraceManagerWrapper();
    private TraceManager manager;
    private boolean enabled;

    private TraceManagerWrapper() {
        try {
            Class<?> tracerManagerClass = Class
                    .forName(TRACER_MANAGER_CLASS)
                    .asSubclass(TraceManager.class);
            manager = (TraceManager) tracerManagerClass.newInstance();
            enabled = true;
        } catch (Throwable t) {
            enabled = false;
        }
    }

    public static TraceManagerWrapper getInstance() {
        return instance;
    }

    public void startSpan(Context context) {
        BTracer rBTracer = context.getRootBTracer();
        BTracer aBTracer = context.getActiveBTracer();

        if (enabled && aBTracer.isTraceable()) {
            String service = aBTracer.getServiceName();
            String resource = aBTracer.getResourceName();

            if (aBTracer.isClientContext()) {
                CallableUnitInfo cInfo = context.getControlStack()
                        .getCurrentFrame().getCallableUnitInfo();

                if (cInfo != null && cInfo instanceof ActionInfo &&
                        ((ActionInfo) cInfo).getConnectorInfo() != null) {
                    ActionInfo aInfo = (ActionInfo) cInfo;
                    service = aInfo.getConnectorInfo().getType().toString();
                    resource = aInfo.getName();
                } else if (cInfo != null) {
                    service = cInfo.getPkgPath();
                    resource = cInfo.getName();
                } else {
                    service = "ballerina:connector";
                    resource = "ballerina:call";
                }

                aBTracer.setServiceName(service);
                aBTracer.setResourceName(resource);
            }

            Long invocationId;
            if (rBTracer.getInvocationID() == null ||
                    STR_NULL.equalsIgnoreCase(rBTracer.getInvocationID())) {
                rBTracer.generateInvocationID();
                invocationId = Long.valueOf(rBTracer.getInvocationID());
            } else {
                invocationId = Long.valueOf(rBTracer.getInvocationID());
            }
            aBTracer.setInvocationID(String.valueOf(invocationId));

            // get the parent spans' span context
            Map<String, String> spanHeaders = rBTracer
                    .getProperties()
                    .entrySet()
                    .stream()
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
            aBTracer.setParentSpanContext(spanContext);
        }
    }

    public void finishSpan(BTracer bTracer) {
        if (enabled && bTracer.isTraceable()) {
            manager.finishSpan(new ArrayList<>(bTracer.getSpans().values()));
        }
    }

    public void log(BTracer bTracer, Map<String, Object> fields) {
        if (enabled && bTracer.isTraceable()) {
            manager.log(new ArrayList<>(bTracer.getSpans().values()), fields);
        }
    }

    public void addTags(BTracer bTracer, Map<String, String> tags) {
        if (enabled && bTracer.isTraceable()) {
            manager.addTags(new ArrayList<>(bTracer.getSpans().values()), tags);
        }
    }

    private Map<String, String> addTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> TRACE_PREFIX + e.getKey(),
                        Map.Entry::getValue)
                );
    }

    private Map<String, String> removeTracePrefix(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().replaceFirst(TRACE_PREFIX, ""),
                        Map.Entry::getValue));
    }

}
