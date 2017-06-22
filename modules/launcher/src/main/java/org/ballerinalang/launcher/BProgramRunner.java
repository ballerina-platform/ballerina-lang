/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.launcher;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Executes the main function of a Ballerina program.
 *
 * @since 0.8.0
 */
class BProgramRunner {
    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));
    private static PrintStream outStream = System.out;

    static void runMain(Path sourceFilePath, List<String> args) {
        ProgramFile programFile = new BLangProgramLoader().loadMainProgramFile(programDirPath, sourceFilePath);

        // Load Client Connectors
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        // Check whether there is a main function
        new BLangProgramRunner().runMain(programFile, args.toArray(new String[0]));
        Runtime.getRuntime().exit(0);
    }

    static void runServices(Path[] serviceFilePaths) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        // TODO : Fix this properly.
        BLangRuntimeRegistry.getInstance().initialize();
        for (Path servicePath : serviceFilePaths) {
            // TODO Handle errors
            ProgramFile programFile = new BLangProgramLoader().loadServiceProgramFile(programDirPath, servicePath);

            outStream.println("ballerina: deploying service(s) in '" + servicePath + "'");
            new BLangProgramRunner().startServices(programFile);
        }

        try {
            List<ServerConnector> startedConnectors = BallerinaConnectorManager.getInstance()
                    .startPendingConnectors();
            startedConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " +
                    serverConnector));
        } catch (ServerConnectorException e) {
            throw new RuntimeException("error starting server connectors: " + e.getMessage(), e);
        }

    }
}
