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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.observability.tracer.BSpan;
import org.ballerinalang.config.ConfigRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.KEY_OBSERVER_CONTEXT;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ENTRYPOINT_FUNCTION_MODULE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_ENTRYPOINT_FUNCTION_POSITION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_SRC_CLIENT_REMOTE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_SRC_MAIN_FUNCTION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_SRC_SERVICE_REMOTE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_SRC_SERVICE_RESOURCE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_IS_SRC_WORKER;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_FUNCTION_NAME;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_MODULE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_OBJECT_NAME;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_POSITION;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_RESOURCE_ACCESSOR;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_RESOURCE_PATH;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_TRUE_VALUE;
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
     * @param env                    Ballerina environment
     * @param module                 The module the resource belongs to
     * @param position               The source code position the resource in defined in
     * @param serviceName            Name of the service to which the observer context belongs
     * @param resourcePathOrFunction Full path of the resource
     * @param resourceAccessor       Accessor of the resource
     * @param isResource             True if this was a resource function invocation
     * @param isRemote               True if this was a remote function invocation
     */
    public static void startResourceObservation(Environment env, BString module, BString position,
                                                BString serviceName, BString resourcePathOrFunction,
                                                BString resourceAccessor, boolean isResource, boolean isRemote) {
        if (!enabled) {
            return;
        }

        ObserverContext observerContext = getObserverContextOfCurrentFrame(env);
        if (observerContext == null) {  // No context created by listener
            observerContext = new ObserverContext();
            setObserverContextToCurrentFrame(env, observerContext);
        }

        if (observerContext.isStarted()) { // If a remote or resource was called by user code itself
            ObserverContext newObserverContext = new ObserverContext();
            setObserverContextToCurrentFrame(env, newObserverContext);

            newObserverContext.setEntrypointFunctionModule(observerContext.getEntrypointFunctionModule());
            newObserverContext.setEntrypointFunctionPosition(observerContext.getEntrypointFunctionPosition());
            newObserverContext.setParent(observerContext);
            observerContext = newObserverContext;
        } else {    // If created now or the listener created to add more tags
            observerContext.setEntrypointFunctionModule(module.getValue());
            observerContext.setEntrypointFunctionPosition(position.getValue());
        }
        observerContext.setServiceName(serviceName.getValue());

        if (isResource) {
            observerContext.setOperationName(resourceAccessor.getValue() + " " + resourcePathOrFunction.getValue());

            observerContext.addTag(TAG_KEY_IS_SRC_SERVICE_RESOURCE, TAG_TRUE_VALUE);
            observerContext.addTag(TAG_KEY_SRC_RESOURCE_ACCESSOR, resourceAccessor.getValue());
            observerContext.addTag(TAG_KEY_SRC_RESOURCE_PATH, resourcePathOrFunction.getValue());
        } else if (isRemote) {
            observerContext.setOperationName(serviceName.getValue() + ":" + resourcePathOrFunction.getValue());

            observerContext.addTag(TAG_KEY_IS_SRC_SERVICE_REMOTE, TAG_TRUE_VALUE);
            observerContext.addTag(TAG_KEY_SRC_FUNCTION_NAME, resourcePathOrFunction.getValue());
        } else {
            observerContext.setOperationName(serviceName.getValue() + ":" + resourcePathOrFunction.getValue());

            observerContext.addTag(TAG_KEY_SRC_FUNCTION_NAME, resourcePathOrFunction.getValue());
        }
        observerContext.addTag(TAG_KEY_SRC_OBJECT_NAME, serviceName.getValue());

        observerContext.addTag(TAG_KEY_SRC_MODULE, module.getValue());
        observerContext.addTag(TAG_KEY_SRC_POSITION, position.getValue());

        if (observerContext.getEntrypointFunctionModule() != null) {
            observerContext.addTag(TAG_KEY_ENTRYPOINT_FUNCTION_MODULE,
                    observerContext.getEntrypointFunctionModule());
        }
        if (observerContext.getEntrypointFunctionPosition() != null) {
            observerContext.addTag(TAG_KEY_ENTRYPOINT_FUNCTION_POSITION,
                    observerContext.getEntrypointFunctionPosition());
        }

        observerContext.setServer();
        observerContext.setStarted();
        for (BallerinaObserver observer : observers) {
            observer.startServerObservation(observerContext);
        }
    }

    /**
     * Stop observation of an observer context.
     *
     * @param env Ballerina environment
     */
    public static void stopObservation(Environment env) {
        if (!enabled) {
            return;
        }
        ObserverContext observerContext = getObserverContextOfCurrentFrame(env);
        if (observerContext == null) {
            return;
        }

        if (observerContext.isServer()) {
            observers.forEach(observer -> observer.stopServerObservation(observerContext));
        } else {
            observers.forEach(observer -> observer.stopClientObservation(observerContext));
        }
        setObserverContextToCurrentFrame(env, observerContext.getParent());
        observerContext.setFinished();
    }

    /**
     * Report an error to an observer context.
     *
     * @param env        Ballerina environment
     * @param errorValue the error value to be attached to the observer context
     */
    public static void reportError(Environment env, ErrorValue errorValue) {
        if (!enabled) {
            return;
        }
        ObserverContext observerContext = getObserverContextOfCurrentFrame(env);
        if (observerContext == null) {
            return;
        }
        observers.forEach(observer -> {
            observerContext.addTag(ObservabilityConstants.TAG_KEY_ERROR, TAG_TRUE_VALUE);
            observerContext.addProperty(ObservabilityConstants.PROPERTY_BSTRUCT_ERROR, errorValue);
        });
    }

    /**
     * Start observability for the synchronous function/action invocations.
     *
     * @param env              Ballerina environment
     * @param module           The module the resource belongs to
     * @param position         The source code position the resource in defined in
     * @param typeDef          The type definition the function was attached to
     * @param functionName     name of the function being invoked
     * @param isMainEntryPoint True if this was a main entry point invocation
     * @param isRemote         True if this was a remote function invocation
     * @param isWorker         True if this was a worker start
     */
    public static void startCallableObservation(Environment env, BString module, BString position,
                                                BObject typeDef, BString functionName, boolean isMainEntryPoint,
                                                boolean isRemote, boolean isWorker) {
        if (!enabled) {
            return;
        }

        ObserverContext prevObserverCtx = getObserverContextOfCurrentFrame(env);
        ObserverContext newObContext = new ObserverContext();
        setObserverContextToCurrentFrame(env, newObContext);

        if (prevObserverCtx != null) {
            newObContext.setServiceName(prevObserverCtx.getServiceName());
            newObContext.setEntrypointFunctionModule(prevObserverCtx.getEntrypointFunctionModule());
            newObContext.setEntrypointFunctionPosition(prevObserverCtx.getEntrypointFunctionPosition());
            newObContext.setParent(prevObserverCtx);
        } else {
            newObContext.setServiceName(UNKNOWN_SERVICE);
            newObContext.setEntrypointFunctionModule(module.getValue());
            newObContext.setEntrypointFunctionPosition(position.getValue());
        }

        if (isMainEntryPoint) {
            newObContext.addTag(TAG_KEY_IS_SRC_MAIN_FUNCTION, TAG_TRUE_VALUE);
        } else if (isRemote) {
            newObContext.addTag(TAG_KEY_IS_SRC_CLIENT_REMOTE, TAG_TRUE_VALUE);
        } else if (isWorker) {
            newObContext.addTag(TAG_KEY_IS_SRC_WORKER, TAG_TRUE_VALUE);
        }   // Else normal function

        if (typeDef != null) {
            ObjectType type = typeDef.getType();
            Module typeModule = type.getPackage();
            String objectName = typeModule.getOrg() + "/" + typeModule.getName() + "/" + type.getName();

            newObContext.setOperationName(objectName + ":" + functionName.getValue());
            newObContext.addTag(TAG_KEY_SRC_OBJECT_NAME, objectName);
        } else {
            newObContext.setOperationName(functionName.getValue());
        }

        newObContext.addTag(TAG_KEY_SRC_FUNCTION_NAME, functionName.getValue());
        newObContext.addTag(TAG_KEY_SRC_MODULE, module.getValue());
        newObContext.addTag(TAG_KEY_SRC_POSITION, position.getValue());

        if (newObContext.getEntrypointFunctionModule() != null) {
            newObContext.addTag(TAG_KEY_ENTRYPOINT_FUNCTION_MODULE, newObContext.getEntrypointFunctionModule());
        }
        if (newObContext.getEntrypointFunctionPosition() != null) {
            newObContext.addTag(TAG_KEY_ENTRYPOINT_FUNCTION_POSITION, newObContext.getEntrypointFunctionPosition());
        }

        newObContext.setStarted();
        for (BallerinaObserver observer : observers) {
            observer.startClientObservation(newObContext);
        }
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
    @Deprecated     // Discussion: https://groups.google.com/g/ballerina-dev/c/VMEk3t8boH0
    public static void logMessageToActiveSpan(String logLevel, Supplier<String> logMessage,
                                              boolean isError) {
        if (!tracingEnabled) {
            return;
        }
        Environment balEnv = new Environment(Scheduler.getStrand());
        ObserverContext observerContext = (ObserverContext) balEnv.getStrandLocal(KEY_OBSERVER_CONTEXT);
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
     * @param env current env
     * @return observer context of the current frame
     */
    public static ObserverContext getObserverContextOfCurrentFrame(Environment env) {
        if (!enabled) {
            return null;
        }

        return (ObserverContext) env.getStrandLocal(KEY_OBSERVER_CONTEXT);
    }

    /**
     * Set the observer context to the current frame.
     *
     * @param env             current env
     * @param observerContext observer context to be set
     */
    public static void setObserverContextToCurrentFrame(Environment env, ObserverContext observerContext) {
        if (!enabled) {
            return;
        }
        env.setStrandLocal(KEY_OBSERVER_CONTEXT, observerContext);
    }
}
