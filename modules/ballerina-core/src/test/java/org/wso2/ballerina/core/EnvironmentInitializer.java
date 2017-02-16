/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core;

import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.dispatching.HTTPResourceDispatcher;
import org.wso2.ballerina.core.runtime.dispatching.HTTPServiceDispatcher;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;
import org.wso2.ballerina.core.utils.BTestUtils;

/**
 * {@code EnvironmentInitializr} is responsible for initializing an environment for a particular ballerina file.
 */
public class EnvironmentInitializer {

    public static BLangProgram setup(String sourcePath) {
        // Initialize server connectors before starting the test cases
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().registerServerConnectorErrorHandler(new TestErrorHandler());
        // Resister HTTP Dispatchers
        DispatcherRegistry.getInstance().registerServiceDispatcher(new HTTPServiceDispatcher());
        DispatcherRegistry.getInstance().registerResourceDispatcher(new HTTPResourceDispatcher());

        // Load constructors
        BuiltInNativeConstructLoader.loadConstructs();

        BLangProgram bLangProgram = BTestUtils.parseBalFile(sourcePath);

        for (BLangPackage servicePackage : bLangProgram.getPackages()) {
            for (Service service : servicePackage.getServices()) {
                service.setBLangProgram(bLangProgram);
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(service));
            }
        }

        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
        bLangProgram.setRuntimeEnvironment(runtimeEnv);

        return bLangProgram;
    }

    public static void cleanup(BLangProgram bLangProgram) {

        for (BLangPackage bLangPackage : bLangProgram.getPackages()) {
            for (Service service : bLangPackage.getServices()) {
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) -> {
                    dispatcher.serviceUnregistered(service);
                });
            }
        }
        DispatcherRegistry.getInstance().clearDispatchers();
    }

}
