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
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.tracer.BSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.tracer.TraceConstants.KEY_SPAN;

/**
 * Util class used for observability.
 *
 * @since 0.985.0
 */
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

    /**
     * Add metrics and tracing observers.
     *
     * @param observer metrics or tracing observer
     */
    public static void addObserver(BallerinaObserver observer) {
        observers.add(observer);
    }

    /**
     * Start observability for the resource invocation.
     *
     * @param strand strand
     */
    public static void startResourceObservation(Strand strand) {
        if (!enabled) {
            return;
        }
        ObserverContext observerContext = strand.respCallback.getObserverContext();
        observerContext.setServer();
        observerContext.setStarted();
        strand.currentFrame.observerContext = observerContext;
        observers.forEach(observer -> observer.startServerObservation(observerContext));
    }

    /**
     * Stop observation of an observer context.
     *
     * @param observerContext observer context to be stopped
     */
    public static void stopObservation(ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
        }
        observerContext.setFinished();
    }

    /**
     * Start observability for the synchronous function/action invocations.
     *
     * @param strand current frame
     * @param flags  action invocation flags
     */
    public static void startCallableObservation(Strand strand, int flags) {
        if (!enabled) {
            return;
        }
        StackFrame previousFrame = strand.peekFrame(1);
        ObserverContext parentCtx = previousFrame == null ?
                strand.respCallback.getObserverContext() : previousFrame.observerContext;
        if (!FunctionFlags.isObserved(flags)) {
            strand.currentFrame.observerContext = parentCtx;
            return;
        }
        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(parentCtx);
        newObContext.setStarted();
        newObContext.setConnectorName(strand.currentFrame.callableUnitInfo.attachedToType.toString());
        newObContext.setActionName(strand.currentFrame.callableUnitInfo.getName());
        newObContext.setServiceName(parentCtx != null ?
                                            parentCtx.getServiceName() : ObservabilityConstants.UNKNOWN_SERVICE);
        strand.currentFrame.observerContext = newObContext;
        observers.forEach(observer -> observer.startClientObservation(newObContext));

    }

    /**
     * Start observability for the asynchronous function/action invocations.
     *
     * @param strand    callee strand
     * @param parentCtx parent observer context
     */
    public static void startCallableObservation(Strand strand, ObserverContext parentCtx) {
        if (!enabled) {
            return;
        }
        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(parentCtx);
        newObContext.setStarted();
        String connectorName = strand.currentFrame.callableUnitInfo.attachedToType != null ?
                strand.currentFrame.callableUnitInfo.attachedToType.toString() : "ballerina:worker";
        newObContext.setConnectorName(connectorName);
        newObContext.setActionName(strand.currentFrame.callableUnitInfo.getName());
        newObContext.setServiceName(parentCtx.getServiceName());
        strand.currentFrame.observerContext = newObContext;
        strand.respCallback.setObserverContext(newObContext);
        observers.forEach(observer -> observer.startClientObservation(newObContext));
    }

    /**
     * Stop observability for both synchronous and asynchronous function/action invocations.
     *
     * @param strand current strand
     */
    public static void stopCallableObservation(Strand strand) {
        if (!enabled) {
            return;
        }

        if (!FunctionFlags.isObserved(strand.currentFrame.invocationFlags)) {
            strand.peekFrame(1).observerContext = strand.currentFrame.observerContext;
            return;
        }
        stopObservation(strand.currentFrame.observerContext);
    }

    /**
     * Get the full service name.
     *
     * @param serviceInfo service info
     * @return service name
     */
    public static String getFullServiceName(ServiceInfo serviceInfo) {
        BType serviceInfoType = serviceInfo.getType();
        return serviceInfoType.getPackagePath().equals(PACKAGE_SEPARATOR) ? serviceInfoType.getName()
                : serviceInfoType.getPackagePath() + PACKAGE_SEPARATOR + serviceInfoType.getName();
    }

    /**
     * Get context properties of the observer context.
     *
     * @param observerContext observer context
     * @return property map
     */
    public static Map<String, String> getContextProperties(ObserverContext observerContext) {
        BSpan bSpan = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }

    /**
     * Log the provided message to the active span.
     *
     * @param context    current context
     * @param logLevel   log level
     * @param logMessage message to be logged
     * @param isError    if its an error or not
     */
    public static void logMessageToActiveSpan(Context context, String logLevel, Supplier<String> logMessage,
                                              boolean isError) {
        if (!tracingEnabled) {
            return;
        }
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
        if (!observerContext.isPresent()) {
            return;
        }
        BSpan span = (BSpan) observerContext.get().getProperty(KEY_SPAN);
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

    /**
     * Check if observability is enabled or not.
     *
     * @return true if observability is enabled else false
     */
    public static boolean isObservabilityEnabled() {
        return enabled;
    }

    /**
     * Get observer context of the current frame.
     *
     * @param context current context
     * @return observer context of the current frame
     */
    public static Optional<ObserverContext> getObserverContextOfCurrentFrame(Context context) {
        return enabled ? Optional.of(context.getStrand().currentFrame.observerContext) : Optional.empty();
    }

    /**
     * Set the observer context to the current frame.
     *
     * @param strand          current strand
     * @param observerContext observer context to be set
     */
    public static void setObserverContextToCurrentFrame(Strand strand, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        strand.currentFrame.observerContext = observerContext;
    }
}
