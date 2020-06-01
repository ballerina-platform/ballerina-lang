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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.STATUS_CODE_GROUP_SUFFIX;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE_GROUP;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_RESOURCE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_SERVICE;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_SPAN;

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
     * This is used in the BString mode in the compiler.
     *
     * @param serviceName name of the service to which the observer context belongs.
     * @param resourceName name of the resource being invoked.
     * @param tags tags to be used in the observation
     */
    public static void startResourceObservation(BString serviceName, BString resourceName,
                                                MapValue<BString, BString> tags) {
        if (!enabled) {
            return;
        }
        MapValue<String, String> stringTags = new MapValueImpl<>();
        for (Map.Entry<BString, BString> tagEntry : tags.entrySet()) {
            stringTags.put(tagEntry.getKey().getValue(), tagEntry.getValue().getValue());
        }
        startResourceObservation(serviceName.getValue(), resourceName.getValue(), stringTags);
    }

    /**
     * Start observation of a resource invocation.
     *
     * @param serviceName name of the service to which the observer context belongs.
     * @param resourceName name of the resource being invoked.
     * @param tags tags to be used in the observation
     */
    public static void startResourceObservation(String serviceName, String resourceName,
                                                MapValue<String, String> tags) {
        if (!enabled) {
            return;
        }

        ObserverContext observerContext;
        Strand strand = Scheduler.getStrand();
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
        for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
            observerContext.addMainTag(tagEntry.getKey(), tagEntry.getValue());
        }
        observers.forEach(observer -> observer.startServerObservation(strand.observerContext));
        strand.setProperty(ObservabilityConstants.SERVICE_NAME, serviceName);
    }

    /**
     * Stop observation of an observer context.
     */
    public static void stopObservation() {
        Strand strand = Scheduler.getStrand();
        if (!enabled || strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;

        Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
        if (statusCode != null) {
            observerContext.addTag(TAG_KEY_HTTP_STATUS_CODE_GROUP, (statusCode / 100) + STATUS_CODE_GROUP_SUFFIX);
        }

        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
        }
        setObserverContextToCurrentFrame(strand, observerContext.getParent());
        observerContext.setFinished();
    }

    /**
     * Report an error to an observer context.
     *
     * @param errorValue the error value to be attached to the observer context.
     */
    public static void reportError(ErrorValue errorValue) {
        Strand strand = Scheduler.getStrand();
        if (!enabled || strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;
        observers.forEach(observer -> {
            observerContext.addTag(ObservabilityConstants.TAG_KEY_ERROR, ObservabilityConstants.TAG_ERROR_TRUE_VALUE);
            observerContext.addProperty(ObservabilityConstants.PROPERTY_BSTRUCT_ERROR, errorValue);
        });
    }

    /**
     * Start observability for the synchronous function/action invocations.
     * This is used in the BString mode in the compiler.
     *
     * @param serviceName name of the service to which the observer context belongs.
     * @param resourceName name of the resource being invoked.
     * @param tags tags to be used in the observation
     */
    public static void startCallableObservation(BString serviceName, BString resourceName,
                                                MapValue<BString, BString> tags) {
        if (!enabled) {
            return;
        }
        MapValue<String, String> stringTags = new MapValueImpl<>();
        for (Map.Entry<BString, BString> tagEntry : tags.entrySet()) {
            stringTags.put(tagEntry.getKey().getValue(), tagEntry.getValue().getValue());
        }
        startCallableObservation(serviceName.getValue(), resourceName.getValue(), stringTags);
    }

    /**
     * Start observability for the synchronous function/action invocations.
     *
     * @param connectorName name of the connector to which the observer context belongs.
     * @param actionName name of the action/function being invoked.
     * @param tags tags to be used in the observation
     */
    public static void startCallableObservation(String connectorName, String actionName,
                                                MapValue<String, String> tags) {
        if (!enabled) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        ObserverContext observerCtx = strand.observerContext;

        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(observerCtx);
        newObContext.setStarted();
        newObContext.setServiceName(observerCtx == null ? UNKNOWN_SERVICE : observerCtx.getServiceName());
        newObContext.setResourceName(observerCtx == null ? UNKNOWN_RESOURCE : observerCtx.getResourceName());
        newObContext.setConnectorName(connectorName);
        newObContext.setActionName(actionName);
        for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
            newObContext.addMainTag(tagEntry.getKey(), tagEntry.getValue());
        }
        setObserverContextToCurrentFrame(strand, newObContext);
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
     * @param logLevel   log level
     * @param logMessage message to be logged
     * @param isError    if its an error or not
     */
    public static void logMessageToActiveSpan(String logLevel, Supplier<String> logMessage,
                                              boolean isError) {
        if (!tracingEnabled) {
            return;
        }
        Strand strand = Scheduler.getStrand();
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
