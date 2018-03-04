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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TraceManagerWrapper to manage tracing.
 */
public class TraceManagerWrapper {
    private static TraceManagerWrapper instance = new TraceManagerWrapper();
    private TraceManager manager;
    private boolean enabled;

    private TraceManagerWrapper() {
        try {
            Class<?> tracerManagerClass = Class.forName(TraceConstants.TRACER_MANAGER_CLASS)
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

    public String startSpan(Context context) {
        BTracer rTraceCtx = context.getRootBTracer();
        BTracer aTraceCtx = context.getActiveBTracer();

        if (enabled && aTraceCtx.isTraceable()) {

            String service = aTraceCtx.getServiceName();
            String resource = aTraceCtx.getResourceName();

            if (aTraceCtx.isClientContext()) {
                CallableUnitInfo cInfo = context.getControlStack().getCurrentFrame().getCallableUnitInfo();
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
                aTraceCtx.setServiceName(service);
                aTraceCtx.setResourceName(resource);
                aTraceCtx.setSpanName(resource);
            }

            Long invocationId;
            if (rTraceCtx.getInvocationID() == null) {
                rTraceCtx.generateInvocationID();
                invocationId = Long.valueOf(rTraceCtx.getInvocationID());
            } else {
                invocationId = Long.valueOf(rTraceCtx.getInvocationID());
            }
            aTraceCtx.setInvocationID(String.valueOf(invocationId));

            // get the parent spans' span context
            Map<String, String> spanHeaders = rTraceCtx.getProperties().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
            Map<String, Object> spanContext = manager.extract(null, removeTracePrefix(spanHeaders), service);

            List<Object> spanList = manager.buildSpan(invocationId, resource, spanContext,
                    aTraceCtx.getTags(), true, service);

            Map<String, String> traceContextMap = manager.inject(manager.getActiveSpanMap(service), null, service);
            aTraceCtx.getProperties().putAll(addTracePrefix(traceContextMap));

            if (spanContext != null && !spanContext.isEmpty()) {
                return SpanHolder.getInstance().onBuildSpan(String.valueOf(invocationId), spanList, spanContext);
            } else {
                return SpanHolder.getInstance().onBuildSpan(String.valueOf(invocationId), spanList, null);
            }
        }
        return null;
    }

    public void finishSpan(BTracer tContext) {
        if (enabled && tContext.isTraceable()) {
            Long invocationId = Long.valueOf(tContext.getInvocationID());
            SpanHolder.SpanReference spanRef = SpanHolder.getInstance()
                    .onFinishSpan(String.valueOf(invocationId), tContext.getSpanId());
            manager.finishSpan(spanRef.getSpans(), spanRef.getParent(), tContext.getServiceName());
        }
    }

    public void log(BTracer tContext, Map<String, Object> fields) {
        if (enabled && tContext.isTraceable()) {
            Long invocationId = Long.valueOf(tContext.getInvocationID());
            SpanHolder.SpanReference spanRef = SpanHolder.getInstance()
                    .getSpanReference(String.valueOf(invocationId), tContext.getSpanId());
            manager.log(spanRef.getSpans(), fields);
        }
    }

    public void addTags(BTracer tContext, Map<String, String> tags) {
        if (enabled && tContext.isTraceable()) {
            Long invocationId = Long.valueOf(tContext.getInvocationID());
            SpanHolder.SpanReference spanRef = SpanHolder.getInstance()
                    .getSpanReference(String.valueOf(invocationId), tContext.getSpanId());
            manager.addTags(spanRef.getSpans(), tags);
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
