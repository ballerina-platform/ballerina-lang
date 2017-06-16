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

package org.ballerinalang.testutils;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Service;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        for (BLangPackage servicePackage : bLangProgram.getPackages()) {
            bLangProgram.addServicePackage(servicePackage);
            for (Service service : servicePackage.getServices()) {
                service.setBLangProgram(bLangProgram);
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(service));
            }
        }

        return bLangProgram;
    }


    public static ProgramFile setupProgramFile(String sourcePath) {
        // Initialize server connectors before starting the test cases
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().registerServerConnectorErrorHandler(new TestErrorHandler());

        // Load constructors
        BuiltInNativeConstructLoader.loadConstructs();
        try {
            Path programPath = Paths.get(EnvironmentInitializer.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI());

            ProgramFile programFile = new BLangProgramLoader().loadServiceProgramFile(programPath, Paths.get
                    (sourcePath));
            new BLangProgramRunner().startServices(programFile);
            return programFile;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }

    public static void cleanup(ProgramFile programFile) {

        for (String servicePackageName : programFile.getServicePackageNameList()) {
            PackageInfo packageInfo = programFile.getPackageInfo(servicePackageName);
            for (ServiceInfo service : packageInfo.getServiceInfoList()) {
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) -> {
                    dispatcher.serviceUnregistered(service);
                });
            }
        }
    }

}
