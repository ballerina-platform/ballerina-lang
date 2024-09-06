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

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.values.FPValue;

import java.util.Map;

/**
 * External API to be used by the interop users to control Ballerina runtime behavior.
 *
 * @since 1.0.0
 */
public abstract class Runtime {

    /**
     * Returns an instance of Ballerina runtime for the given module.
     *
     * @param module Module instance.
     * @return Ballerina runtime instance.
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
     * call a Ballerina function.
     *
     * @param module Module of the function.
     * @param functionName Name of the function.
     * @param args         Arguments of the Ballerina function.
     */
    public abstract Object call(Module module, String functionName, Object... args);


    /**
     * Call a Ballerina object method.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param args       Arguments of the Ballerina function.
     */
    public abstract Object call(BObject object, String methodName, Object... args);

    /**
     * Invoke function as a worker. Caller needs to ensure that no data race is possible for
     * the mutable state with given object method and with arguments. So, the method can be concurrently run with
     * different os threads.
     *
     * @param module Module of the function.
     * @param functionName Name of the function.
     * @param strandName   Name for newly created strand which is used to execute the function pointer. This is
     *                     optional and can be null.
     * @param metadata     Meta data of new strand.
     * @param properties   Set of properties for strand.
     * @param args         Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    public abstract BFuture startIsolatedWorker(Module module, String functionName, String strandName,
                                                StrandMetadata metadata, Map<String, Object> properties,
                                                Object... args);

    /**
     * Invoke Object method as a worker. Caller needs to ensure that no data race is possible for
     * the mutable state with given object method and with arguments. So, the method can be concurrently run with
     * different os threads.
     *
     * @param fp         Function pointer to be run as non isolated worker.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param properties Set of properties for strand.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    public abstract BFuture startIsolatedWorker(FPValue fp, String strandName, StrandMetadata metadata,
                                                Map<String, Object> properties, Object... args);

    /**
     * Invoke Object method as a worker. Caller needs to ensure that no data race is possible for
     * the mutable state with given object method and with arguments. So, the method can be concurrently run with
     * different os threads.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param properties Set of properties for strand.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if both object.getType().isIsolated() and
     * object.getType().isIsolated(methodName) returns true.
     */
    public abstract BFuture startIsolatedWorker(BObject object, String methodName, String strandName,
                                                StrandMetadata metadata, Map<String, Object> properties,
                                                Object... args);

    /**
     * Invoke function as worker in same parent thread. This method will ensure that the object methods are
     * invoked in the same thread where other object methods are executed. So, the methods will be executed
     * sequentially per object level.
     *
     * @param module Module of the function.
     * @param functionName Name of the function.
     * @param strandName   Name for newly created strand which is used to execute the function pointer. This is
     *                     optional and can be null.
     * @param metadata     Meta data of new strand.
     * @param properties   Set of properties for strand.
     * @param args         Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if object.getType().isIsolated() or
     * object.getType().isIsolated(methodName) returns false.
     */
    public abstract BFuture startNonIsolatedWorker(Module module, String functionName, String strandName,
                                                   StrandMetadata metadata, Map<String, Object> properties,
                                                   Object... args);

    /**
     * Invoke Object method as a worker in same parent thread. This method will ensure that the object methods are
     * invoked in the same thread where other object methods are executed. So, the methods will be executed
     * sequentially per object level.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param properties Set of properties for strand.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if object.getType().isIsolated() or
     * object.getType().isIsolated(methodName) returns false.
     */
    public abstract BFuture startNonIsolatedWorker(BObject object, String methodName, String strandName,
                                                   StrandMetadata metadata, Map<String, Object> properties,
                                                   Object... args);

    /**
     * Invoke Object method as a worker in same parent thread. This method will ensure that the object methods are
     * invoked in the same thread where other object methods are executed. So, the methods will be executed
     * sequentially per object level.
     *
     * @param fp         Function pointer to be run as non isolated worker.
     * @param strandName Name for newly created strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param properties Set of properties for strand.
     * @param args       Ballerina function arguments.
     * @return {@link BFuture} containing return value for executing this method.
     * <p>
     * This method needs to be called if object.getType().isIsolated() or
     * object.getType().isIsolated(methodName) returns false.
     */
    public abstract BFuture startNonIsolatedWorker(FPValue fp, String strandName, StrandMetadata metadata,
                                                       Map<String, Object> properties, Object... args);

    public abstract void registerListener(BObject listener);

    public abstract void deregisterListener(BObject listener);

    public abstract void registerStopHandler(BFunctionPointer stopHandler);
}
