/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.configurable.providers.ConfigDetails;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.CONFIGURATION_CLASS_NAME;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;

/**
 * Internal implementation of the API used by the interop users to control Ballerina runtime behavior.
 *
 * @since 2201.6.0
 */
public class BalRuntime extends Runtime {

    private final Scheduler scheduler;
    private final Module module;
    private boolean moduleInitialized = false;

    public BalRuntime(Scheduler scheduler, Module module) {
        this.scheduler = scheduler;
        this.module = module;
    }

    public BalRuntime(Module module) {
        this.scheduler = new Scheduler(false);
        this.module = module;
    }

    public void init() {
        invokeConfigInit();
        invokeMethodAsync("$moduleInit", null, PredefinedTypes.TYPE_NULL, "init", new Object[1]);
        moduleInitialized = true;
    }

    public void start() {
        if (!moduleInitialized) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.INVALID_METHOD_CALL, "start");
        }
        invokeMethodAsync("$moduleStart", null, PredefinedTypes.TYPE_NULL, "start", new Object[1]);
    }

    public void invokeMethodAsync(String functionName, Callback callback, Object... args) {
        if (!moduleInitialized) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.INVALID_FUNCTION_INVOCATION, functionName);
        }
        invokeMethodAsync(functionName, callback, PredefinedTypes.TYPE_ANY, functionName, args);
    }

    public void stop() {
        if (!moduleInitialized) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.INVALID_METHOD_CALL, "stop");
        }
        invokeMethodAsync("$moduleStop", null, PredefinedTypes.TYPE_NULL, "stop", new Object[1]);
    }

    private void invokeMethodAsync(String functionName, Callback callback, Type returnType, String strandName,
                                   Object... args) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                module.getName(), module.getMajorVersion(), module.isTestPkg()));
        Function<?, ?> func = o -> valueCreator.call((Strand) (((Object[]) o)[0]), functionName, args);
        FutureValue future = scheduler.createFuture(null, callback, null, returnType, strandName, null);
        Object[] argsWithStrand = new Object[args.length + 1];
        argsWithStrand[0] = future.strand;
        System.arraycopy(args, 0, argsWithStrand, 1, args.length);
        scheduler.schedule(argsWithStrand, func, future);
        scheduler.start();
    }

    /**
     * Invoke Object method asynchronously and sequentially. This method will ensure that the object methods are
     * invoked in the same thread where other object methods are executed. So, the methods will be executed
     * sequentially per object level.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notified once the method execution is done.
     * @param properties Set of properties for strand.
     * @param returnType Expected return type of this method.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if object.getType().isIsolated() or
     * object.getType().isIsolated(methodName) returns false.
     */
    public BFuture invokeMethodAsyncSequentially(BObject object, String methodName, String strandName,
                                                 StrandMetadata metadata,
                                                 Callback callback, Map<String, Object> properties,
                                                 Type returnType, Object... args) {
        try {
            validateArgs(object, methodName);
            ObjectValue objectVal = (ObjectValue) object;
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, objectVal, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, objectVal, methodName);
                    scheduler.scheduleToObjectGroup(new Object[1], func, future);
                }
                @Override
                public void notifyFailure(BError error) {
                    callback.notifyFailure(error);
                }
            }, args);
            return future;
        } catch (BError e) {
            callback.notifyFailure(e);
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
        }
        return null;
    }

    /**
     * Invoke Object method asynchronously and concurrently. Caller needs to ensure that no data race is possible for
     * the mutable state with given object method and with arguments. So, the method can be concurrently run with
     * different os threads.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notified once the method execution is done.
     * @param properties Set of properties for strand.
     * @param returnType Expected return type of this method.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    public BFuture invokeMethodAsyncConcurrently(BObject object, String methodName, String strandName,
                                                 StrandMetadata metadata,
                                                 Callback callback, Map<String, Object> properties,
                                                 Type returnType, Object... args) {
        try {
            validateArgs(object, methodName);
            ObjectValue objectVal = (ObjectValue) object;
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, objectVal, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, objectVal, methodName);
                    scheduler.schedule(new Object[1], func, future);
                }
                @Override
                public void notifyFailure(BError error) {
                    callback.notifyFailure(error);
                }
            }, args);
            return future;
        } catch (BError e) {
            callback.notifyFailure(e);
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
        }
        return null;
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     * This API checks whether the object or object method is isolated. So, if an object method is isolated, method
     * will be concurrently executed in different os threads.
     * <p>
     * Caller needs to ensure that no data race is possible for the mutable state with given arguments. So, the
     * method can be concurrently run with different os threads.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly creating strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notify once method execution done.
     * @param properties Set of properties for strand
     * @param returnType Expected return type of this method
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * @deprecated If caller can ensure that given object and object method is isolated and no data race is possible
     * for the mutable state with given arguments, use @invokeMethodAsyncConcurrently
     * otherwise @invokeMethodAsyncSequentially .
     * <p>
     * We can decide the object method isolation if and only if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    @Deprecated
    public BFuture invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                     Callback callback, Map<String, Object> properties,
                                     Type returnType, Object... args) {
        try {
            validateArgs(object, methodName);
            ObjectValue objectVal = (ObjectValue) object;
            ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(objectVal.getType());
            boolean isIsolated = objectType.isIsolated() && objectType.isIsolated(methodName);
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, objectVal, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, objectVal, methodName);
                    if (isIsolated) {
                        scheduler.schedule(new Object[1], func, future);
                    } else {
                        scheduler.scheduleToObjectGroup(new Object[1], func, future);
                    }
                }
                @Override
                public void notifyFailure(BError error) {
                    callback.notifyFailure(error);
                }
            }, args);
            return future;
        } catch (BError e) {
            callback.notifyFailure(e);
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
        }
        return null;
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is optional
     *                   and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notified once the method execution is done.
     * @param args       Ballerina function arguments.
     * @return the result of the function invocation.
     * @deprecated If caller can ensure that given object and object method is isolated and no data race is possible
     * for the mutable state with given arguments, use @invokeMethodAsyncConcurrently
     * otherwise @invokeMethodAsyncSequentially .
     * <p>
     * We can decide the object method isolation if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    @Deprecated
    public Object invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                    Callback callback, Object... args) {
        return invokeMethodAsync(object, methodName, strandName, metadata, callback, null,
                                 PredefinedTypes.TYPE_NULL, args);
    }

    private void validateArgs(BObject object, String methodName) {
        if (object == null) {
            throw ErrorCreator.createError(StringUtils.fromString("object cannot be null"));
        }
        if (methodName == null) {
            throw ErrorCreator.createError(StringUtils.fromString("method name cannot be null"));
        }
    }

    public void registerListener(BObject listener) {
        scheduler.getRuntimeRegistry().registerListener(listener);
    }

    public void deregisterListener(BObject listener) {
        scheduler.getRuntimeRegistry().deregisterListener(listener);
    }

    public void registerStopHandler(BFunctionPointer<?, ?> stopHandler) {
        scheduler.getRuntimeRegistry().registerStopHandler(stopHandler);
    }

    private Function<?, ?> getFunction(Object[] argsWithDefaultValues, ObjectValue objectVal, String methodName) {
        Function<?, ?> func;
        if (argsWithDefaultValues.length == 1) {
            func = o -> objectVal.call((Strand) (((Object[]) o)[0]), methodName, argsWithDefaultValues[0]);
        } else {
            func = o -> objectVal.call((Strand) (((Object[]) o)[0]), methodName, argsWithDefaultValues);
        }
        return func;
    }

    private void invokeConfigInit() {
        String configClassName = getConfigClassName(this.module);
        Class<?> configClazz;
        try {
            configClazz = Class.forName(configClassName);
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString("failed to load configuration class :" +
                    configClassName));
        }
        ConfigDetails configDetails = LaunchUtils.getConfigurationDetails();
        String funcName = Utils.encodeFunctionIdentifier("$configureInit");
        try {
            final Method method = configClazz.getDeclaredMethod(funcName, String[].class, Path[].class, String.class);
            method.invoke(null, new String[]{}, configDetails.paths, configDetails.configContent);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw ErrorCreator.createError(StringUtils.fromString("configurable initialization failed due to " +
                    RuntimeUtils.formatErrorMessage(e)), e);
        }
    }

    private static String getConfigClassName(Module module) {
        String configClassName = CONFIGURATION_CLASS_NAME;
        String orgName = module.getOrg();
        String packageName = module.getName();
        if (!DOT.equals(packageName)) {
            configClassName = encodeNonFunctionIdentifier(packageName) + "." + module.getMajorVersion() + "." +
                    configClassName;
        }
        if (!ANON_ORG.equals(orgName)) {
            configClassName = encodeNonFunctionIdentifier(orgName) + "." +  configClassName;
        }
        return configClassName;
    }
}
