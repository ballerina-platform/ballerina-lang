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

import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.BalRuntime;
import org.jetbrains.annotations.Nullable;

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
    public abstract Object init();

    /**
     * Start the listening phase.
     */
    public abstract Object start();

    /**
     * Gracefully shuts down the Ballerina runtime.
     * The `gracefulStop` method of each registered listener and the functions registered with
     * `runtime:onGracefulStop` will be called within this method.
     */
    public abstract void stop();

    /**
     * call a Ballerina function.
     *
     * @param module         Module of the function.
     * @param functionName   Name of the function.
     * @param metadata Meta data of new strand.
     * @param args           Arguments of the Ballerina function.
     */
    public abstract Object callFunction(Module module, String functionName, StrandMetadata metadata,
                                        Object... args);

    /**
     * Call a Ballerina object method.
     *
     * @param object         Object Value.
     * @param methodName     Name of the method.
     * @param metadata Meta data of new strand.
     * @param args           Arguments of the Ballerina function.
     */
    public abstract Object callMethod(BObject object, String methodName, @Nullable StrandMetadata metadata,
                                      Object... args);

    /**
     * Register a Ballerina listener object in runtime.
     * @param listener Ballerina Listener object.
     */
    public abstract void registerListener(BObject listener);

    /**
     * Deregister a Ballerina listener object in runtime.
     * @param listener Ballerina Listener object.
     */
    public abstract void deregisterListener(BObject listener);

    /**
     * Register a stop handler function in runtime.
     * @param stopHandler Ballerina stop handler function.
     */
    public abstract void registerStopHandler(BFunctionPointer stopHandler);
}
