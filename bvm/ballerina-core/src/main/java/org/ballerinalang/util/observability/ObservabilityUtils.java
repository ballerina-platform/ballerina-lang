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

import org.ballerinalang.bre.bvm.ObservableContext;
import org.ballerinalang.util.tracer.BSpan;
import org.ballerinalang.util.tracer.TraceManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
     * @param connectorName    The server connector name.
     * @param serviceName      The service name.
     * @param resourceName     The resource name.
     * @param observableContext The {@link ObservableContext} instance. If this is null when starting the
     *                         observation, the {@link #continueServerObservation(ObserverContext, ObservableContext)}
     *                          method must be called later with relevant {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startServerObservation(String connectorName, String serviceName, String resourceName,
                                                         ObservableContext observableContext) {
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = (observableContext != null)
                ? getCurrentContext(observableContext)
                : new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setServiceName(serviceName);
        ctx.setResourceName(resourceName);
        if (observableContext != null) {
            continueServerObservation(ctx, observableContext);
        }
        return ctx;
    }

    /**
     * Start a client observation.
     *
     * @param connectorName    The connector name.
     * @param actionName       The action name.
     * @param observableContext The {@link ObservableContext} instance. If this is null when starting the
     *                         observation, the {@link #continueClientObservation(ObserverContext, ObservableContext)}
     *                          method must be called later with relevant {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startClientObservation(String connectorName, String actionName,
                                                         ObservableContext observableContext) {
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = (observableContext != null)
                ? getCurrentContext(observableContext)
                : new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setActionName(actionName);
        if (observableContext != null) {
            continueClientObservation(ctx, observableContext);
        }
        return ctx;
    }

    /**
     * Continue server observation if the
     * {@link #startServerObservation(String, String, String, ObservableContext)} was called without
     * {@link ObservableContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param observableContext The {@link ObservableContext} instance.
     */
    public static void continueServerObservation(ObserverContext observerContext, ObservableContext observableContext) {
        Objects.requireNonNull(observableContext);
        if (observerContext == null) {
            // This context may be null in some cases. Get new context
            observerContext = getCurrentContext(observableContext);
        } else {
            // Update the current context. ObservableContext may not be available when starting the observation.
            // Therefore, it is very important to set this ObserverContext in the ObservableContext.
            setCurrentContext(observerContext, observableContext);
        }
        observerContext.setServer();
        observerContext.setStarted();
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startServerObservation(ctx, observableContext));
    }

    /**
     * Continue client observation if the {@link #startClientObservation(String, String, ObservableContext)} was
     * called without {@link ObservableContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param observableContext The {@link ObservableContext} instance.
     */
    public static void continueClientObservation(ObserverContext observerContext, ObservableContext observableContext) {
        Objects.requireNonNull(observableContext);
        if (observerContext == null) {
            // This context may be null in some cases. Get new context
            observerContext = getCurrentContext(observableContext);
        } else {
            // Update the current context
            setCurrentContext(observerContext, observableContext);
        }
        observerContext.setStarted();
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startClientObservation(ctx, observableContext));
    }

    /**
     * Stop server or client observation.
     *
     * @param observableContext The {@link ObservableContext} instance.
     */
    public static void stopObservation(ObservableContext observableContext) {
        Objects.requireNonNull(observableContext);
        ObserverContext observerContext = getCurrentContext(observableContext);
        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext, observableContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext, observableContext));
        }
    }

    /**
     * Get an {@link ObserverContext} for current {@link ObservableContext}.
     *
     * @param observableContext The {@link ObservableContext} instance.
     * @return An existing {@link ObserverContext} instance or a new one created for current
     * {@link ObservableContext}.
     */
    public static ObserverContext getCurrentContext(ObservableContext observableContext) {
        Objects.requireNonNull(observableContext);
        ObserverContext context = (ObserverContext) observableContext.getLocalProperty(KEY_OBSERVER_CONTEXT);
        if (context == null) {
            context = new ObserverContext();
            observableContext.setLocalProperty(KEY_OBSERVER_CONTEXT, context);
        }
        return context;
    }

    /**
     * Get the get the trace context properties of the parent span.
     * @return {@link Map} of trace context properties.
     */
    public static Map<String, String> getTraceProperties() {
        BSpan bSpan = TraceManager.getInstance().getParentBSpan();
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }

    /**
     * Set the {@link ObserverContext} in current {@link ObservableContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param observableContext The {@link ObservableContext} instance.
     */
    private static void setCurrentContext(ObserverContext observerContext, ObservableContext observableContext) {
        Objects.requireNonNull(observerContext);
        Objects.requireNonNull(observableContext);
        observableContext.setLocalProperty(KEY_OBSERVER_CONTEXT, observerContext);
    }
}
