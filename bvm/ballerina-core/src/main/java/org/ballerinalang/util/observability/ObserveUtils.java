/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.util.observability;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.vm.StackFrame;
import org.ballerinalang.bre.vm.Strand;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.tracer.BSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.tracer.TraceConstants.KEY_SPAN;

public class ObserveUtils {
    private static final List<BallerinaObserver> observers = new CopyOnWriteArrayList<>();
    private static final boolean enabled;
    private static final boolean tracingEnabled;
    private static final String PACKAGE_SEPARATOR = ".";

    static {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        tracingEnabled = configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED);
        enabled = configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED) || tracingEnabled;
    }

    public static void addObserver(BallerinaObserver observer) {
        observers.add(observer);
    }

    public static void startResourceObservation(Strand strand, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        observerContext.setServer();
        observerContext.setStarted();
        strand.observerContext = observerContext;
        strand.currentFrame.observerContext = observerContext;
        observers.forEach(observer -> observer.startServerObservation(observerContext));
    }

    public static void stopObservation(ObserverContext observerContext) {
        if (!enabled || observerContext == null) {
            return;
        }
        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
        }
        observerContext.setFinished();
    }

    public static void startCallableObservation(Strand strand, int flags) {
        if (!enabled) {
            return;
        }
        StackFrame previousFrame = strand.peekFrame(1);
        ObserverContext parentCtx = previousFrame == null ? strand.observerContext : previousFrame.observerContext;
        if (!FunctionFlags.isObserved(flags)) {
            strand.currentFrame.observerContext = parentCtx;
            return;
        }
        ObserverContext newObContext = new ObserverContext();
        newObContext.strandName = strand.getId();
        newObContext.callableName = strand.currentFrame.callableUnitInfo.getName();
        newObContext.setParent(parentCtx);
        newObContext.setStarted();
        newObContext.setConnectorName(strand.currentFrame.callableUnitInfo.attachedToType.toString());
        newObContext.setActionName(strand.currentFrame.callableUnitInfo.getName());
        newObContext.setServiceName(parentCtx.getServiceName());
        strand.currentFrame.observerContext = newObContext;
        strand.respCallback.setObserverContext(newObContext);
        observers.forEach(observer -> observer.startClientObservation(newObContext));

    }

    public static void stopCallableObservation(Strand strand) {
        if (!enabled || strand.observerContext ==  null) {
            return;
        }

        if (!FunctionFlags.isObserved(strand.currentFrame.flags)) {
            return;
        }
        stopObservation(strand.currentFrame.observerContext);
    }

    public static String getFullServiceName(ServiceInfo serviceInfo) {
        BType serviceInfoType = serviceInfo.getType();
        return serviceInfoType.getPackagePath().equals(PACKAGE_SEPARATOR) ? serviceInfoType.getName()
                : serviceInfoType.getPackagePath() + PACKAGE_SEPARATOR + serviceInfoType.getName();
    }

    public static Map<String, String> getContextProperties(ObserverContext observerContext) {
        BSpan bSpan = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }

    public static void logMessageToActiveSpan(Context context, String logLevel, Supplier<String> logMessage,
                                              boolean isError) {
        if (!tracingEnabled) {
            return;
        }
        ObserverContext observerContext = getParentObserverContext(context);
        if (observerContext == null) {
            return;
        }
        BSpan span = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (span == null) {
            return;
        }
        HashMap<String, Object> logs = new HashMap<>(1);
        logs.put(logLevel, logMessage.get());
        if (!isError) {
            span.log(logs);
        } else {
            span.logError(logs);
        }
    }

    public static boolean isObservabilityEnabled() {
        return enabled;
    }

    // TODO: 11/30/18 Check the usages of this method
    public static ObserverContext getParentObserverContext(Context context) {
        if (enabled) {
            return context.getStrand().currentFrame.observerContext;
        }
        return  new ObserverContext();
    }

    // TODO: 11/30/18 Check the usages of this method
    public static void setObserverContextToCurrentFrame(Strand str, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        str.currentFrame.observerContext = observerContext;
    }
}
