/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.runtime;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;

/**
 * This class contains the implementation of the "register" and "deregister" Ballerina functions in ballerina/runtime
 * module.
 *
 * @since 2.0.0
 */
public class Registry {

    public static void registerListener(Environment env, BObject listener) {
        env.getRuntime().registerListener(listener);
    }

    public static void deregisterListener(Environment env, BObject listener) {
        env.getRuntime().deregisterListener(listener);
    }

    public static void onGracefulStop(Environment env, BFunctionPointer<?, ?> stopHandler) {
        env.getRuntime().registerStopHandler(stopHandler);
    }

    private Registry() {
    }
}
