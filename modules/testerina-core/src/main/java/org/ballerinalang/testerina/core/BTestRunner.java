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

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.testerina.core.entity.TesterinaContext;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.entity.TesterinaFunctionResult;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.util.debugger.DebugManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * BTestRunner entity class
 */
public class BTestRunner {

    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));
    private static PrintStream outStream = System.err;
    private static TesterinaReport tReport = new TesterinaReport();

    public static void runTest(Path[] sourceFilePaths) {
        outStream.println("------ Test Program Path set to : " + System.getProperty("user.dir") + " -------");
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        BLangProgram[] bLangPrograms = Arrays.stream(sourceFilePaths).map(BTestRunner::buildTestModel)
                .toArray(BLangProgram[]::new);
        Arrays.stream(bLangPrograms).forEachOrdered(bLangProgram -> {
            TesterinaRegistry.getInstance().addBLangProgram(bLangProgram);
            // Create a runtime environment for this Ballerina application
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
            bLangProgram.setRuntimeEnvironment(runtimeEnv);
        });

        if (ModeResolver.getInstance().isDebugEnabled()) {
            DebugManager debugManager = DebugManager.getInstance();
            // This will start the websocket server.
            debugManager.init();
        }

        //this.tReport = new

        executeTestFunctions(bLangPrograms);
        tReport.printTestSummary();
        Runtime.getRuntime().exit(0);
    }

    private static BLangProgram buildTestModel(Path sourceFilePath) {
        BLangProgram bLangProgram = new BLangProgramLoader().loadService(programDirPath, sourceFilePath);
        BLangPackage[] servicePackages = bLangProgram.getServicePackages();
        if (servicePackages.length == 0) {
            throw new BallerinaException("no service(s) found in '" + bLangProgram.getProgramFilePath() + "'");
        }

        // Create a runtime environment for this Ballerina application
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
        bLangProgram.setRuntimeEnvironment(runtimeEnv);
        return bLangProgram;
    }

    private static void executeTestFunctions(BLangProgram[] bLangPrograms) {
        TesterinaContext tFile = new TesterinaContext(bLangPrograms);
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
                        "Error while running the before test function: '" + tFunction.getName() + "'. **** ERROR : " +
                                e.getBException().value().getMessage().stringValue());
            }
        }

        //test
        for (TesterinaFunction tFunction : testFunctions) {
            outStream.println("----------------------------------------------------------------------------");
            boolean isTestPassed = true;
            String testMessage = "Test Passed";
            try {
                outStream.println("Started running test '" + tFunction.getName() + "'...");
                tFunction.invoke();
                outStream.println("Finished running test '" + tFunction.getName() + "'.");
            } catch (BallerinaException e) {
                isTestPassed = false;
                testMessage = e.getBException().value().getMessage().stringValue();
                outStream.println(
                        "Error while running the function: '" + tFunction.getName() + "'. **** ERROR : " +
                                e.getBException().value().getMessage().stringValue());

            }
            // if there are no exception thrown, test is passed
            TesterinaFunctionResult functionResult = new TesterinaFunctionResult(tFunction.getName(),
                    isTestPassed, testMessage);
            tReport.addFunctionResult(functionResult);
        }
        outStream.println("----------------------------------------------------------------------------");

        //after test
        for (TesterinaFunction tFunction : afterTestFunctions) {
            try {
                tFunction.invoke();
            } catch (BallerinaException e) {
                outStream.println(
                        "Error while running the after test function: '" + tFunction.getName() + "'. **** ERROR : " +
                                e.getBException().value().getMessage().stringValue());
            }
        }


    }

}
