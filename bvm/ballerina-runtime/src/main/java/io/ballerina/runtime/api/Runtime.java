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
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.Map;

/**
 * External API to be used by the interop users to control Ballerina runtime behavior.
 *
 * @since 1.0.0
 */
public abstract class Runtime {

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    /**
     * Gets the instance of Ballerina runtime.
     *
     * @return      Ballerina runtime instance.
     * @deprecated  use {@link Environment#getRuntime()} instead.
     */
    @Deprecated(forRemoval = true)
    public static Runtime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new BalRuntime(strand.scheduler, null);
    }

    /**
     * Returns an instance of Ballerina runtime for the given module.
     *
     * @param module    Module instance.
     * @return          Ballerina runtime instance.
     */
    public static Runtime from(Module module) {
        return new BalRuntime(module);
    }

    /**
     * Performs the module initialization.
     */
    public abstract void init();

    /**
     * Starts the listening phase.
     */
    public abstract void start();

    /**
     * Gracefully shuts down the Ballerina runtime.
     * The `gracefulStop` method of each registered listener and the functions registered with
     * `runtime:onGracefulStop` will be called within this method.
     */
    public abstract void stop();

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
    public abstract BFuture invokeMethodAsyncSequentially(BObject object, String methodName, String strandName,
                                          StrandMetadata metadata, Callback callback, Map<String, Object> properties,
                                          Type returnType, Object... args);

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
    public abstract BFuture invokeMethodAsyncConcurrently(BObject object, String methodName, String strandName,
                                          StrandMetadata metadata, Callback callback, Map<String, Object> properties,
                                          Type returnType, Object... args);

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
    public abstract BFuture invokeMethodAsync(BObject object, String methodName, String strandName,
                                              StrandMetadata metadata, Callback callback,
                                              Map<String, Object> properties, Type returnType, Object... args);

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
    public abstract Object invokeMethodAsync(BObject object, String methodName, String strandName,
                                             StrandMetadata metadata, Callback callback, Object... args);

    public abstract void registerListener(BObject listener);

    public abstract void deregisterListener(BObject listener);

    public abstract void registerStopHandler(BFunctionPointer<?, ?> stopHandler);

    /**
     * Invoke a Ballerina function pointer asynchronously.
     *
     * @param functionName  Name of the function which needs to be invoked.
     * @param callback      Callback which will get notified once the function execution is done.
     * @param args          Arguments of the Ballerina function.
     */
    public abstract void invokeMethodAsync(String functionName, Callback callback, Object... args);
}
