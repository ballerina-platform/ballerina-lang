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

package org.ballerinalang.core;

import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.builder.BLangExecutionFlowBuilder;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;

/**
 * {@code EnvironmentInitializr} is responsible for initializing an environment for a particular ballerina file.
 */
public class EnvironmentInitializer {

    public static BLangProgram setup(String sourcePath) {
        // Initialize server connectors before starting the test cases
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().registerServerConnectorErrorHandler(new TestErrorHandler());

        // Load constructors
        BuiltInNativeConstructLoader.loadConstructs();

        BLangProgram bLangProgram = BTestUtils.parseBalFile(sourcePath);

        BLangExecutionFlowBuilder flowBuilder = new BLangExecutionFlowBuilder();
        for (BLangPackage servicePackage : bLangProgram.getPackages()) {
            for (Service service : servicePackage.getServices()) {
                service.setBLangProgram(bLangProgram);
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(service));
                service.accept(flowBuilder);
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
    }

}
