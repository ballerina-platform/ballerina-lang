/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.observability;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.observability.tracer.BSpan;
import org.ballerinalang.jvm.types.BServiceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_SPAN;

/**
 * Utility methods to start server/client observation.
 */
public class ObservabilityUtils {

    private static final List<BallerinaObserver> observers = new CopyOnWriteArrayList<>();

    private static final boolean enabled;
    private static final boolean tracingEnabled;
    private static final String PACKAGE_SEPARATOR = ".";

    static {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        tracingEnabled = configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED);
        enabled = configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED) ||
                tracingEnabled;
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
     * @param connectorName The server connector name.
     * @param serviceType   Service type.
     * @param resourceName  The resource name.
     * @param parentStrand The {@link Strand} instance. If this is null when starting the
     *                      observation, the
     *                      {@link #continueServerObservation(ObserverContext, Strand)}
     *                      method must be called later with relevant {@link ObserverContext}
     * @return An {@link Optional} {@link ObserverContext} instance.
     */
    public static Optional<ObserverContext> startServerObservation(String connectorName, BServiceType serviceType,
                                                                   String resourceName,
                                                                   Strand parentStrand) {
        if (!enabled) {
            return Optional.empty();
        }
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setServiceName(getFullServiceName(serviceType));

        ctx.setResourceName(resourceName);
        if (parentStrand != null) {
            continueServerObservation(ctx, parentStrand);
        }
        return Optional.of(ctx);
    }

    /**
     * Start a client observation.
     *
     * @param connectorName The connector name.
     * @param actionName    The action name.
     * @param parentStrand     The {@link Strand} instance. If this is null when starting the
     *                      observation, the
     *                      {@link #continueClientObservation(ObserverContext, Strand)}
     *                      method must be called later with relevant {@link ObserverContext}
     * @return An {@link Optional} of {@link ObserverContext} instance.
     */
    public static Optional<ObserverContext> startClientObservation(String connectorName, String actionName,
                                                                   Strand parentStrand) {
        if (!enabled) {
            return Optional.empty();
        }
        Objects.requireNonNull(connectorName);
        ObserverContext ctx = new ObserverContext();
        ctx.setConnectorName(connectorName);
        ctx.setActionName(actionName);
        if (parentStrand != null) {
            //TODO fix - rajith
//            ServiceInfo serviceInfo = BLangVMUtils.getServiceInfo(parentCtx);
//            if (serviceInfo != null) {
//                ctx.setServiceName(getFullServiceName(serviceInfo));
//            } else {
//                ctx.setServiceName(UNKNOWN_SERVICE);
//            }
            continueClientObservation(ctx, parentStrand);
        }
        return Optional.of(ctx);
    }

    /**
     * Continue server observation if the
     * {@link #startServerObservation(String, BServiceType, String, Strand)} was called
     * without {@link Strand}.
     *
     * @param observerContext The {@link ObserverContext} instance.
     * @param parentStrand       The parent {@link Strand} instance.
     */
    public static void continueServerObservation(ObserverContext observerContext, Strand parentStrand) {
        if (!enabled) {
            return;
        }
        Objects.requireNonNull(parentStrand);
        ObserverContext parentObserverContext = populateAndGetParentObserverContext(parentStrand);
        observerContext.setParent(parentObserverContext);
        observerContext.setServer();
        observerContext.setStarted();
        final ObserverContext ctx = observerContext;
        observers.forEach(observer -> observer.startServerObservation(ctx));
    }

    /**
     * Continue client observation if the {@link #startClientObservation(String, String, Strand)} was
     * called without {@link Strand}.
     *
     * @param observerContext The {@link ObserverContext} instance.
     * @param parentStrand       The {@link Strand} instance.
     */
    public static void continueClientObservation(ObserverContext observerContext, Strand parentStrand) {
        if (!enabled) {
            return;
        }
        Objects.requireNonNull(parentStrand);
        ObserverContext parentObserverContext = populateAndGetParentObserverContext(parentStrand);
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
        observerContext.setFinished();
    }

//    /**
//     * @param context The {@link Context} instance.
//     * @return the parent {@link ObserverContext} or a new {@link ObserverContext} depending on whether observability
//     * is enabled or not.
//     */
//    public static Optional<ObserverContext> getParentContext(Context context) {
//        return enabled ? Optional.of(populateAndGetParentObserverContext(context.getParentStrand()))
//                : Optional.empty();
//    }

    public static Map<String, String> getContextProperties(ObserverContext observerContext) {
        BSpan bSpan = (BSpan) observerContext.getProperty(KEY_SPAN);
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }

    public static void setObserverContextToStrand(Strand strand, ObserverContext observerContext) {
        if (!enabled || observerContext == null) {
            return;
        }
        strand.observerContext = observerContext;
    }

    private static ObserverContext populateAndGetParentObserverContext(Strand parentStrand) {
        List<Strand> ancestors = new ArrayList<>();
        Strand parent = parentStrand;
        while (parent != null) {
            if (parent.observerContext != null) {
                break;
            } else {
                ancestors.add(parent);
            }
            parent = parent.parent;
        }
        ObserverContext observerContext = new ObserverContext();
        while (observerContext.isFinished() && observerContext.getParent() != null) {
            observerContext = observerContext.getParent();
        }
        ObserverContext currentObserverContext = observerContext;
        ancestors.forEach(w -> {
            w.observerContext = currentObserverContext;
        });
        return observerContext;
    }

    public static String getFullServiceName(BServiceType serviceType) {
        return serviceType.getPackage().name.equals(PACKAGE_SEPARATOR)
                ? serviceType.getName()
                : serviceType.getPackage().name + PACKAGE_SEPARATOR + serviceType.getName();
    }

//    public static void logMessageToActiveSpan(Context context, String logLevel, Supplier<String> logMessage,
//                                              boolean isError) {
//        if (tracingEnabled) {
//            Optional<ObserverContext> observerContext = ObservabilityUtils.getParentContext(context);
//            if (observerContext.isPresent()) {
//                BSpan span = (BSpan) observerContext.get().getProperty(KEY_SPAN);
//                if (span != null) {
//                    HashMap<String, Object> logs = new HashMap<>(1);
//                    logs.put(logLevel, logMessage.get());
//                    if (!isError) {
//                        span.log(logs);
//                    } else {
//                        span.logError(logs);
//                    }
//                }
//            }
//        }
//    }
}
