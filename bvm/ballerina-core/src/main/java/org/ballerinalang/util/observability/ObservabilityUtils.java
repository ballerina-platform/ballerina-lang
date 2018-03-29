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
package org.ballerinalang.util.observability;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ballerinalang.util.observability.ObservabilityConstants.KEY_OBSERVER_CONTEXT;

/**
 * Utility methods to start server/client observation.
 */
public class ObservabilityUtils {

    private static final List<BallerinaObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Adds a {@link BallerinaObserver} to a collection of observers that will be notified on
     * events.
     *
     * @param observer the observer that will be notified
     */
    public static void addObserver(BallerinaObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes a {@link BallerinaObserver} from the collection of observers.
     *
     * @param observer the observer that will be removed
     */
    public static void removeObserver(BallerinaObserver observer) {
        observers.remove(observer);
    }

    /**
     * Start a server observation.
     *
     * @param serviceName      The service name.
     * @param resourceName     The resource name.
     * @param executionContext The {@link WorkerExecutionContext} instance. If this is null when starting the
     *                         observation, the {@link #continueServerObservation(ObserverContext, String, String,
     *                         WorkerExecutionContext)} method must be called later with relevant
     *                         {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startServerObservation(String serviceName, String resourceName,
                                                         WorkerExecutionContext executionContext) {
        ObserverContext ctx;
        if (executionContext != null) {
            ctx = getCurrentContext(executionContext);
            continueServerObservation(ctx, serviceName, resourceName, executionContext);
        } else {
            ctx = new ObserverContext();
        }
        return ctx;
    }

    /**
     * Start a client observation.
     *
     * @param connectorName    The connector name.
     * @param actionName       The action name.
     * @param executionContext The {@link WorkerExecutionContext} instance. If this is null when starting the
     *                         observation, the {@link #continueClientObservation(ObserverContext, String, String,
     *                         WorkerExecutionContext)} method must be called later with relevant
     *                         {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startClientObservation(String connectorName, String actionName,
                                                         WorkerExecutionContext executionContext) {
        ObserverContext ctx;
        if (executionContext != null) {
            ctx = getCurrentContext(executionContext);
            continueClientObservation(ctx, connectorName, actionName, executionContext);
        } else {
            ctx = new ObserverContext();
        }
        return ctx;
    }

    /**
     * Continue server observation if the {@link #startServerObservation(String, String, WorkerExecutionContext)} was
     * called without {@link WorkerExecutionContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param serviceName      The service name.
     * @param resourceName     The resource name.
     * @param executionContext The {@link WorkerExecutionContext} instance.
     */
    public static void continueServerObservation(ObserverContext observerContext, String serviceName,
                                                 String resourceName, WorkerExecutionContext executionContext) {
        if (observerContext == null) {
            // No one has called start observation
            // Get new context
            observerContext = getCurrentContext(executionContext);
        }
        Objects.requireNonNull(executionContext);
        observerContext.setServiceName(serviceName);
        observerContext.setResourceName(resourceName);
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startServerObservation(ctx, executionContext));
    }

    /**
     * Continue client observation if the {@link #startClientObservation(String, String, WorkerExecutionContext)} was
     * called without {@link WorkerExecutionContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param connectorName    The connector name.
     * @param actionName       The resource name.
     * @param executionContext The {@link WorkerExecutionContext} instance.
     */
    public static void continueClientObservation(ObserverContext observerContext, String connectorName,
                                                 String actionName, WorkerExecutionContext executionContext) {
        Objects.requireNonNull(observerContext);
        Objects.requireNonNull(executionContext);
        observerContext.setConnectorName(connectorName);
        observerContext.setActionName(actionName);
        observers.forEach(observer -> observer.startClientObservation(observerContext, executionContext));
    }

    /**
     * Stop server or client observation.
     *
     * @param executionContext The {@link WorkerExecutionContext} instance.
     */
    public static void stopObservation(WorkerExecutionContext executionContext) {
        Objects.requireNonNull(executionContext);
        ObserverContext observerContext = getCurrentContext(executionContext);
        observers.forEach(observer -> observer.stopObservation(observerContext, executionContext));
    }

    /**
     * Get an {@link ObserverContext} for current {@link WorkerExecutionContext}.
     *
     * @param executionContext The {@link WorkerExecutionContext} instance.
     * @return An existing {@link ObserverContext} instance or a new one created for current {@link WorkerExecutionContext}.
     */
    public static ObserverContext getCurrentContext(WorkerExecutionContext executionContext) {
        Objects.requireNonNull(executionContext);
        if (executionContext.localProps == null) {
            executionContext.localProps = new HashMap<>();
        }
        return (ObserverContext) executionContext.localProps.compute(KEY_OBSERVER_CONTEXT, (key, context) -> {
            if (context == null) {
                context = new ObserverContext();
            }
            return context;
        });
    }
}
