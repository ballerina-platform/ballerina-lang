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
package org.ballerinalang.testerina.core;

//import org.ballerinalang.testerina.core.langutils.Functions;

import org.ballerinalang.testerina.core.entity.TesterinaFile;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.langutils.ParserUtils;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;

//import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.values.BValue;

/**
 * TestRunner entity class
 */
public class TestRunner {

    //    private static final Logger logger = java.util.logging.Logger.getLogger("TestRunner");
    private static PrintStream outStream = System.err;

    public static void runTest(Path[] sourceFilePaths) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        Path sourceFilePath = sourceFilePaths[0]; //todo
        BallerinaFile bFile = ParserUtils.buildLangModel(sourceFilePath);

        buildApplicationModel(bFile, sourceFilePath, "default");
        executeTestFunctions(bFile);

        Runtime.getRuntime().exit(0);
    }

    private static void buildApplicationModel(BallerinaFile originalBFile, Path sourceFilePath, String appName) {
        try {
            TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();

            //populate applications without services
            BallerinaFile bFile = getBFileWithoutServices(originalBFile, sourceFilePath);
            testerinaRegistry.addBallerinaFile(originalBFile, bFile);

            // Create a runtime environment for this Ballerina application
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

            // Get the existing application associated with this ballerina config
            Application app = ApplicationRegistry.getInstance().getApplication(appName);
            if (app == null) {
                // Create a new application with ballerina file name, if there is no app currently exists.
                app = new Application(appName);
                app.setRuntimeEnv(runtimeEnv);
                ApplicationRegistry.getInstance().registerApplication(app);
            }

            Package aPackage = app.getPackage(appName);
            if (aPackage == null) {
                // check if package name is null
//                if (bFile.getPackagePath() != null) {
//                    aPackage = new Package(bFile.getPackagePath());
//                } else {
                aPackage = new Package("default");
//                }
                app.addPackage(aPackage);
            }
            aPackage.addFiles(bFile);

            // Here we need to link all the resources with this application. We execute the matching resource
            // when a request is made. At that point, we need to access runtime environment to execute the resource.
            for (Service service : bFile.getServices()) {
                for (Resource resource : service.getResources()) {
               //     resource.setApplication(app); todo 
                }
            }

           ApplicationRegistry.getInstance().updatePackage(aPackage);
            //outStream.println("ballerina: deployed service(s) in " + LauncherUtils.getFileName(serviceFilePath));
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage());
        }

    }

    private static BallerinaFile getBFileWithoutServices(BallerinaFile originalBFile, Path sourceFilePath) {
        BallerinaFile bFile = ParserUtils.buildLangModel(sourceFilePath);
        bFile.setServices(new Service[0]);
        return bFile;
    }

    private static void executeTestFunctions(BallerinaFile bFile) {
        TesterinaFile tFile = new TesterinaFile(bFile);
        ArrayList<TesterinaFunction> testFunctions = tFile.getTestFunctions();
        ArrayList<TesterinaFunction> beforeTestFunctions = tFile.getBeforeTestFunctions();
        ArrayList<TesterinaFunction> afterTestFunctions = tFile.getAfterTestFunctions();

        if (testFunctions.isEmpty()) {
            throw new BallerinaException("No test functions found in the provided ballerina files.");
        }

        //before test
        for (TesterinaFunction tFunction : beforeTestFunctions) {
            try {
                tFunction.invoke();
            } catch (BallerinaException e) {
                outStream.println(
                        "Error while running the before test function: '" + tFunction.getName() + "'. Error : " + e
                                .getMessage());
            }
        }

        //test
        for (TesterinaFunction tFunction : testFunctions) {
            try {
                outStream.println("Started running test '" + tFunction.getName() + "'...");
                tFunction.invoke();
                outStream.println("Finished running test '" + tFunction.getName() + "'.");
            } catch (BallerinaException e) {
                outStream.println(
                        "Error while running the function: '" + tFunction.getName() + "'. Error : " + e.getMessage());
            }
        }

        //after test
        for (TesterinaFunction tFunction : afterTestFunctions) {
            try {
                tFunction.invoke();
            } catch (BallerinaException e) {
                outStream.println(
                        "Error while running the after test function: '" + tFunction.getName() + "'. Error : " + e
                                .getMessage());
            }
        }
    }

}
