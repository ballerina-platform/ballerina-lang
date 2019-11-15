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

package org.ballerinalang.jvm.observability;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.tracer.BSpan;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_CONNECTOR;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_SERVICE;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_SPAN;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.TAG_KEY_SPAN_KIND;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.TAG_SPAN_KIND_SERVER;

/**
 * Util class used for observability.
 *
 * @since 0.985.0
 */
public class ObserveUtils {
    private static final List<BallerinaObserver> observers = new CopyOnWriteArrayList<>();
    private static final boolean enabled;
    private static final boolean metricsEnabled;
    private static final boolean tracingEnabled;

    static {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        tracingEnabled = configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED);
        metricsEnabled = configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED);
        enabled = metricsEnabled || tracingEnabled;
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
     * Start observation of a resource invocation.
     *
     * @param strand which holds the observer context being started.
     * @param serviceName name of the service to which the observer context belongs.
     * @param resourceName name of the resource being invoked.
     */
    public static void startResourceObservation(Strand strand, String serviceName, String resourceName) {
        if (!enabled) {
            return;
        }

        ObserverContext observerContext;
        if (strand.observerContext != null) {
            observerContext = strand.observerContext;
        } else {
            observerContext = new ObserverContext();
            setObserverContextToCurrentFrame(strand, observerContext);
        }
        if (serviceName == null) {
            serviceName = UNKNOWN_SERVICE;
            strand.setProperty(ObservabilityConstants.SERVICE_NAME, serviceName);
        }
        observerContext.setServiceName(serviceName);
        observerContext.setResourceName(resourceName);
        observerContext.setServer();
        observerContext.setStarted();
        observers.forEach(observer -> observer.startServerObservation(strand.observerContext));
        strand.setProperty(ObservabilityConstants.SERVICE_NAME, serviceName);
    }

    /**
     * Stop observation of an observer context.
     *
     * @param strand which holds the observer context.
     */
    public static void stopObservation(Strand strand) {
        if (!enabled || strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;
        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
            setObserverContextToCurrentFrame(strand, observerContext.getParent());
        }
        observerContext.setFinished();
    }

    /**
     * Report an error to an observer context.
     *
     * @param strand which holds the observer context.
     * @param errorValue the error value to be attached to the observer context.
     */
    public static void reportError(Strand strand, ErrorValue errorValue) {
        if (!enabled || strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;
        observers.forEach(observer -> {
            observerContext.addProperty(ObservabilityConstants.PROPERTY_ERROR, Boolean.TRUE);
            observerContext.addProperty(ObservabilityConstants.PROPERTY_BSTRUCT_ERROR, errorValue);
        });
    }

    /**
     * Start observability for the synchronous function/action invocations.
     *
     * @param strand which holds the observer context being started.
     * @param connectorName name of the connector to which the observer context belongs.
     * @param actionName name of the action/function being invoked.
     */
    public static void startCallableObservation(Strand strand, String connectorName, String actionName) {
        if (!enabled) {
            return;
        }

        ObserverContext observerCtx = strand.observerContext;
        // If parent context is null, create a new context and start it as a server.
        if (observerCtx == null) {
            observerCtx = new ObserverContext();
            observerCtx.addTag(TAG_KEY_SPAN_KIND, TAG_SPAN_KIND_SERVER);
            observerCtx.setServiceName(UNKNOWN_SERVICE);
            observerCtx.setConnectorName(UNKNOWN_CONNECTOR);
            observerCtx.setResourceName(UNKNOWN_CONNECTOR);
            observerCtx.setServer();
            observerCtx.setStarted();
            for (BallerinaObserver observer : observers) {
                observer.startServerObservation(observerCtx);
            }
        }

        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(observerCtx);
        newObContext.setStarted();

        newObContext.setServiceName(observerCtx.getServiceName());
        newObContext.setConnectorName(connectorName);
        newObContext.setActionName(actionName);
        strand.observerContext = newObContext;
        observers.forEach(observer -> observer.startClientObservation(newObContext));
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
     * @param strand    current context
     * @param logLevel   log level
     * @param logMessage message to be logged
     * @param isError    if its an error or not
     */
    public static void logMessageToActiveSpan(Strand strand, String logLevel, Supplier<String> logMessage,
                                              boolean isError) {
        if (!tracingEnabled) {
            return;
        }
        Optional<ObserverContext> observerContext = getObserverContextOfCurrentFrame(strand);
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
     * Check if metrics is enabled or not.
     *
     * @return true if metrics is enabled else false
     */
    public static boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    /**
     * Check if tracing is enabled or not.
     *
     * @return true if tracing is enabled else false
     */
    public static boolean isTracingEnabled() {
        return tracingEnabled;
    }

    /**
     * Get observer context of the current frame.
     *
     * @param strand current context
     * @return observer context of the current frame
     */
    public static Optional<ObserverContext> getObserverContextOfCurrentFrame(Strand strand) {
        if (!enabled || strand.observerContext == null) {
            return Optional.empty();
        }
        return Optional.of(strand.observerContext);
    }

    /**
     * Set the observer context to the current frame.
     *
     * @param strand current strand
     * @param observerContext observer context to be set
     */
    public static void setObserverContextToCurrentFrame(Strand strand, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        strand.observerContext = observerContext;
    }
}
