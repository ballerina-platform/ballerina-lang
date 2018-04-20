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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.tracer.BSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.KEY_OBSERVER_CONTEXT;
import static org.ballerinalang.util.tracer.TraceConstants.KEY_SPAN;

/**
 * Utility methods to start server/client observation.
 */
public class ObservabilityUtils {

    private static final List<BallerinaObserver> observers = new CopyOnWriteArrayList<>();

    private static final ObserverContext emptyContext = new ObserverContext();

    private static final boolean enabled;

    static {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        enabled = configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED) ||
                configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED);
    }

    /**
     * @return whether observability is enabled.
     */
    public static boolean isObservabilityEnabled() {
        return enabled;
    }

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
     * @param parentContext    The {@link WorkerExecutionContext} instance. If this is null when starting the
     *                         observation, the
     *                         {@link #continueServerObservation(ObserverContext, WorkerExecutionContext)}
     *                          method must be called later with relevant {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startServerObservation(String connectorName, String serviceName,
                                                         String resourceName, WorkerExecutionContext parentContext) {
        if (!enabled) {
            return null;
        }
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setServiceName(serviceName);
        ctx.setResourceName(resourceName);
        if (parentContext != null) {
            continueServerObservation(ctx, parentContext);
        }
        return ctx;
    }

    /**
     * Start a client observation.
     *
     * @param connectorName    The connector name.
     * @param actionName       The action name.
     * @param parentCtx        The {@link WorkerExecutionContext} instance. If this is null when starting the
     *                         observation, the
     *                         {@link #continueClientObservation(ObserverContext, WorkerExecutionContext)}
     *                         method must be called later with relevant {@link ObserverContext}
     * @return An {@link ObserverContext} instance.
     */
    public static ObserverContext startClientObservation(String connectorName, String actionName,
                                                         WorkerExecutionContext parentCtx) {
        if (!enabled) {
            return null;
        }
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setActionName(actionName);
        if (parentCtx != null) {
            continueClientObservation(ctx, parentCtx);
        }
        return ctx;
    }

    /**
     * Continue server observation if the
     * {@link #startServerObservation(String, String, String, WorkerExecutionContext)} was called
     * without {@link WorkerExecutionContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param parentCtx The parent {@link WorkerExecutionContext} instance.
     */
    public static void continueServerObservation(ObserverContext observerContext, WorkerExecutionContext parentCtx) {
        if (!enabled) {
            return;
        }
        Objects.requireNonNull(parentCtx);
        ObserverContext parentObserverContext = populateAndGetParentObserverContext(parentCtx);
        observerContext.setParent(parentObserverContext);
        observerContext.setServer();
        observerContext.setStarted();
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startServerObservation(ctx));
    }

    /**
     * Continue client observation if the {@link #startClientObservation(String, String, WorkerExecutionContext)} was
     * called without {@link WorkerExecutionContext}.
     *
     * @param observerContext  The {@link ObserverContext} instance.
     * @param parentCtx The {@link WorkerExecutionContext} instance.
     */
    public static void continueClientObservation(ObserverContext observerContext, WorkerExecutionContext parentCtx) {
        if (!enabled) {
            return;
        }
        Objects.requireNonNull(parentCtx);
        ObserverContext parentObserverContext = populateAndGetParentObserverContext(parentCtx);
        observerContext.setParent(parentObserverContext);
        observerContext.setStarted();
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startClientObservation(ctx));
    }

    /**
     * Stop server or client observation.
     *
     * @param observerContext The {@link ObserverContext} instance.
     */
    public static void stopObservation(ObserverContext observerContext) {
        if (!enabled || observerContext == null) {
            return;
        }
        Objects.requireNonNull(observerContext);
        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
        }
    }

    /**
     * @param context The {@link Context} instance.
     * @return the parent {@link ObserverContext} or a new {@link ObserverContext} depending on whether observability
     * is enabled or not.
     */
    public static ObserverContext getParentContext(Context context) {
        return enabled ? populateAndGetParentObserverContext(context.getParentWorkerExecutionContext())
                : new ObserverContext();
    }

    public static Map<String, String> getContextProperties(ObserverContext observerContext) {
        BSpan bSpan = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }

    public static void setObserverContextToWorkerExecutionContext(WorkerExecutionContext workerExecutionContext,
                                                                  ObserverContext observerContext) {
        if (!enabled || observerContext == null) {
            return;
        }
        if (workerExecutionContext.localProps == null) {
            workerExecutionContext.localProps = new HashMap<>();
        }
        workerExecutionContext.localProps.put(KEY_OBSERVER_CONTEXT, observerContext);
    }

    private static ObserverContext populateAndGetParentObserverContext(WorkerExecutionContext parentCtx) {
        List<WorkerExecutionContext> ancestors = new ArrayList<>();
        Object ctx = null;
        WorkerExecutionContext parent = parentCtx;
        while (parent != null) {
            ctx = (parent.localProps != null) ? parent.localProps.get(KEY_OBSERVER_CONTEXT) : null;
            if (ctx != null) {
                break;
            } else {
                ancestors.add(parent);
            }
            parent = parent.parent;
        }
        ObserverContext observerContext = (ctx != null) ? (ObserverContext) ctx : new ObserverContext();
        ancestors.forEach(w -> {
            if (w.localProps == null) {
                w.localProps = new HashMap<>();
            }
            w.localProps.put(KEY_OBSERVER_CONTEXT, observerContext);
        });
        return observerContext;
    }
}
