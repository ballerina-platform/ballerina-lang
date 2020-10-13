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

package io.ballerina.runtime.observability;

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.tracer.BSpan;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.values.ErrorValue;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.config.ConfigRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static io.ballerina.runtime.observability.ObservabilityConstants.STATUS_CODE_GROUP_SUFFIX;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ACTION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_CONNECTOR_NAME;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_FUNCTION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE_GROUP;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_INVOCATION_POSITION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_MAIN_ENTRY_POINT;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_REMOTE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_RESOURCE_ENTRY_POINT;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_WORKER;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_MODULE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_OBJECT_NAME;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_RESOURCE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SERVICE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_TRUE_VALUE;
import static io.ballerina.runtime.observability.ObservabilityConstants.UNKNOWN_RESOURCE;
import static io.ballerina.runtime.observability.ObservabilityConstants.UNKNOWN_SERVICE;
import static io.ballerina.runtime.observability.tracer.TraceConstants.KEY_SPAN;

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
     * @param serviceName name of the service to which the observer context belongs
     * @param resourceName name of the resource being invoked
     * @param pkg The package the resource belongs to
     * @param position The source code position the resource in defined in
     */
    public static void startResourceObservation(BString serviceName, BString resourceName, BString pkg,
                                                BString position) {
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
        String service = serviceName.getValue() == null ? UNKNOWN_SERVICE : serviceName.getValue();
        observerContext.setServiceName(service);
        observerContext.setResourceName(resourceName.getValue());
        observerContext.setServer();

        observerContext.addMainTag(TAG_KEY_MODULE, pkg.getValue());
        observerContext.addMainTag(TAG_KEY_INVOCATION_POSITION, position.getValue());
        observerContext.addMainTag(TAG_KEY_IS_RESOURCE_ENTRY_POINT, TAG_TRUE_VALUE);
        observerContext.addMainTag(TAG_KEY_SERVICE, observerContext.getServiceName());
        observerContext.addMainTag(TAG_KEY_RESOURCE, observerContext.getResourceName());
        observerContext.addMainTag(TAG_KEY_CONNECTOR_NAME, observerContext.getObjectName());

        observerContext.setStarted();
        observers.forEach(observer -> observer.startServerObservation(strand.observerContext));
        strand.setProperty(ObservabilityConstants.SERVICE_NAME, service);
    }

    /**
     * Stop observation of an observer context.
     */
    public static void stopObservation() {
        if (!enabled) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        if (strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;

        Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
        if (statusCode != null && statusCode >= 100) {
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
     * @param errorValue the error value to be attached to the observer context
     */
    public static void reportError(ErrorValue errorValue) {
        if (!enabled) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        if (strand.observerContext == null) {
            return;
        }
        ObserverContext observerContext = strand.observerContext;
        observers.forEach(observer -> {
            observerContext.addTag(ObservabilityConstants.TAG_KEY_ERROR, TAG_TRUE_VALUE);
            observerContext.addProperty(ObservabilityConstants.PROPERTY_BSTRUCT_ERROR, errorValue);
        });
    }

    /**
     * Start observability for the synchronous function/action invocations.
     *
     * @param isRemote True if this was a remove function invocation
     * @param isMainEntryPoint True if this was a main entry point invocation
     * @param isWorker True if this was a worker start
     * @param typeDef The type definition the function was attached to
     * @param functionName name of the function being invoked
     * @param pkg The package the resource belongs to
     * @param position The source code position the resource in defined in
     */
    public static void startCallableObservation(boolean isRemote, boolean isMainEntryPoint, boolean isWorker,
                                                BObject typeDef, BString functionName, BString pkg,
                                                BString position) {
        if (!enabled) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        ObserverContext observerCtx = strand.observerContext;

        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(observerCtx);
        newObContext.setServiceName(observerCtx == null ? UNKNOWN_SERVICE : observerCtx.getServiceName());
        newObContext.setResourceName(observerCtx == null ? UNKNOWN_RESOURCE : observerCtx.getResourceName());
        if (typeDef == null) {
            newObContext.setObjectName(StringUtils.EMPTY);
        } else {
            String className = typeDef.getClass().getCanonicalName();
            String[] classNameSplit = className.split("\\.");
            int lastIndexOfDollar = classNameSplit[3].lastIndexOf('$');
            newObContext.setObjectName(classNameSplit[0] + "/" + classNameSplit[1] + "/"
                    + classNameSplit[3].substring(lastIndexOfDollar + 1));
        }
        newObContext.setFunctionName(functionName.getValue());

        newObContext.addMainTag(TAG_KEY_MODULE, pkg.getValue());
        newObContext.addMainTag(TAG_KEY_INVOCATION_POSITION, position.getValue());
        if (isRemote) {
            newObContext.addMainTag(TAG_KEY_IS_REMOTE, TAG_TRUE_VALUE);
            newObContext.addMainTag(TAG_KEY_ACTION, newObContext.getFunctionName());
            newObContext.addMainTag(TAG_KEY_CONNECTOR_NAME, newObContext.getObjectName());
        }
        if (isMainEntryPoint) {
            newObContext.addMainTag(TAG_KEY_IS_MAIN_ENTRY_POINT, TAG_TRUE_VALUE);
        }
        if (isWorker) {
            newObContext.addMainTag(TAG_KEY_IS_WORKER, TAG_TRUE_VALUE);
        }
        if (!isRemote && !isWorker) {
            newObContext.addMainTag(TAG_KEY_FUNCTION, newObContext.getFunctionName());
            if (!StringUtils.isEmpty(newObContext.getObjectName())) {
                newObContext.addMainTag(TAG_KEY_OBJECT_NAME, newObContext.getObjectName());
            }
        }
        if (!UNKNOWN_SERVICE.equals(newObContext.getServiceName())) {
            // If service is present, resource should be too
            newObContext.addMainTag(TAG_KEY_SERVICE, newObContext.getServiceName());
            newObContext.addMainTag(TAG_KEY_RESOURCE, newObContext.getResourceName());
        }

        newObContext.setStarted();
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
