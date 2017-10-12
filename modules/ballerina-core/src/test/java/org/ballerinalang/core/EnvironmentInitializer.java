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

import org.ballerinalang.BLangCompiler;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.codegen.ProgramFile;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code EnvironmentInitializr} is responsible for initializing an environment for a particular ballerina file.
 */
public class EnvironmentInitializer {

    public static ProgramFile setupProgramFile(String sourcePath) {
        // Initialize server connectors before starting the test cases
        ServerConnectorRegistry.getInstance().initServerConnectors();

        // Load constructors
        BuiltInNativeConstructLoader.loadConstructs();
        try {
            Path programPath = Paths.get(
                    EnvironmentInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            ProgramFile programFile = BLangCompiler.compile(programPath, Paths.get(sourcePath));
            BLangProgramRunner.runService(programFile);
            return programFile;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }
}
