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
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.program.BLangVMUtils;
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
import static org.ballerinalang.util.observability.ObservabilityConstants.UNKNOWN_CONNECTOR;
import static org.ballerinalang.util.observability.ObservabilityConstants.UNKNOWN_SERVICE;
import static org.ballerinalang.util.tracer.TraceConstants.KEY_SPAN;
import static org.ballerinalang.util.tracer.TraceConstants.TAG_KEY_SPAN_KIND;
import static org.ballerinalang.util.tracer.TraceConstants.TAG_SPAN_KIND_SERVER;

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
     * @param strand          strand
     * @param observerContext observer context
     */
    public static void startResourceObservation(Strand strand, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        ObserverContext newObContext = observerContext;
        if (newObContext == null) {
            CallableUnitInfo callableUnitInfo = strand.currentFrame.callableUnitInfo;
            newObContext = new ObserverContext();
            newObContext.setConnectorName(UNKNOWN_CONNECTOR);
            newObContext.setServiceName(getFullServiceName(callableUnitInfo.attachedToType));
            newObContext.setResourceName(callableUnitInfo.getName());
        }
        strand.respCallback.setObserverContext(newObContext);
        newObContext.setServer();
        newObContext.setStarted();
        strand.currentFrame.observerContext = newObContext;
        observers.forEach(observer -> observer.startServerObservation(strand.currentFrame.observerContext));
    }

    /**
     * Get full service name with the package path.
     *
     * @param serviceInfoType service info type
     * @return full qualified service name
     */
    private static String getFullServiceName(BType serviceInfoType) {
        return serviceInfoType.getPackagePath().equals(PACKAGE_SEPARATOR) ? serviceInfoType.getName()
                : serviceInfoType.getPackagePath() + PACKAGE_SEPARATOR + serviceInfoType.getName();
    }

    /**
     * Stop observation of an observer context.
     *
     * @param observerContext observer context to be stopped
     */
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

        // If parent context is null, create a new context and start it as a server.
        if (parentCtx == null) {
            parentCtx = new ObserverContext();
            parentCtx.addTag(TAG_KEY_SPAN_KIND, TAG_SPAN_KIND_SERVER);
            parentCtx.setConnectorName(UNKNOWN_CONNECTOR); // We have to set this explicitly as it'll give errors when
            // monitoring metrics
            parentCtx.setServiceName(UNKNOWN_SERVICE); // We have to set this explicitly as it'll give errors when
            // monitoring metrics
            parentCtx.setResourceName(strand.getId());
            parentCtx.setServer();
            parentCtx.setStarted();
            strand.respCallback.setObserverContext(parentCtx);
            observers.forEach(observer -> observer.startServerObservation(strand.respCallback.getObserverContext()));
        }

        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(parentCtx);
        newObContext.setStarted();
        newObContext.setConnectorName(strand.currentFrame.callableUnitInfo.attachedToType.toString());
        newObContext.setActionName(strand.currentFrame.callableUnitInfo.getName());
        newObContext.setServiceName(getServiceName(strand));
        strand.currentFrame.observerContext = newObContext;
        observers.forEach(observer -> observer.startClientObservation(newObContext));

    }

    /**
     * Get service name from the current strand.
     *
     * @param strand current strand
     * @return service name
     */
    private static String getServiceName(Strand strand) {
        ServiceInfo serviceInfo = BLangVMUtils.getServiceInfo(strand);
        return serviceInfo != null ? getFullServiceName(serviceInfo) : ObservabilityConstants.UNKNOWN_SERVICE;
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
        newObContext.setServiceName(getServiceName(strand));
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

        // Peek the immediate frame at the top and set the parent observer context of the current frame as the observer
        // context
        strand.peekFrame(1).observerContext = strand.currentFrame.observerContext.getParent();
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
        Optional<ObserverContext> observerContext = getObserverContextOfCurrentFrame(context);
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
        if (!enabled || context.getStrand().currentFrame.observerContext == null) {
            return Optional.empty();
        }
        return Optional.of(context.getStrand().currentFrame.observerContext);
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
