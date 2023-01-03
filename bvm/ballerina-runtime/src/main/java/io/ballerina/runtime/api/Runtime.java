/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.api;

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
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.FutureValue;

import java.util.Map;
import java.util.function.Function;

/**
 * External API to be used by the interop users to control Ballerina runtime behavior.
 *
 * @since 1.0.0
 */
public class Runtime {

    private final Scheduler scheduler;

    Runtime(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Gets the instance of ballerina runtime.
     *
     * @return Ballerina runtime instance.
     * @deprecated use {@link Environment#getRuntime()} instead.
     */
    @Deprecated
    public static Runtime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new Runtime(strand.scheduler);
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
     * @return {@link FutureValue} containing return value for executing this method.
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
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, object, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, object, methodName);
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
     * @return {@link FutureValue} containing return value for executing this method.
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
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, object, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, object, methodName);
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
     * @return {@link FutureValue} containing return value for executing this method.
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
            ObjectType objectType = (ObjectType) TypeUtils.getReferredType(object.getType());
            boolean isIsolated = objectType.isIsolated() && objectType.isIsolated(methodName);
            FutureValue future = scheduler.createFuture(null, callback, properties, returnType, strandName, metadata);
            AsyncUtils.getArgsWithDefaultValues(scheduler, object, methodName, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    Function<?, ?> func = getFunction((Object[]) result, object, methodName);
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

    private Function<?, ?> getFunction(Object[] argsWithDefaultValues, BObject object, String methodName) {
        Function<?, ?> func;
        if (argsWithDefaultValues.length == 1) {
            func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, argsWithDefaultValues[0]);
        } else {
            func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, argsWithDefaultValues);
        }
        return func;
    }
}
