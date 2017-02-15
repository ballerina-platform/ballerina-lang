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
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Executes the main function of a Ballerina program
 *
 * @since 0.8.0
 */
class BProgramRunner {
    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));
    private static PrintStream outStream = System.err;

    static void runMain(Path sourceFilePath, List<String> args) {
        BLangProgram bLangProgram = new BLangProgramLoader()
                .loadMain(programDirPath, sourceFilePath);

        // Load Client Connectors
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        // Check whether there is a main function
        new BLangProgramRunner().runMain(bLangProgram, args.toArray(new String[0]));
        Runtime.getRuntime().exit(0);
    }

    static void runServices(Path[] serviceFilePaths) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());

        for (Path servicePath : serviceFilePaths) {
            // TODO Handle errors
            BLangProgram bLangProgram = new BLangProgramLoader().loadService(programDirPath, servicePath);

            outStream.println("ballerina: deploying service(s) in '" + servicePath + "'");
            new BLangProgramRunner().startServices(bLangProgram);
        }

        try {
            BallerinaConnectorManager.getInstance().startPendingConnectors();
        } catch (ServerConnectorException e) {
            outStream.println("Exception occurred while starting server connectors");
            //TODO: Do proper exception handling. Question is should we fail everything if one Connector
            //TODO: fails or should we continue with success ones
        }

    }
}
