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
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.testerina.core.entity.TesterinaContext;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.testerina.core.entity.TesterinaResult;
import org.ballerinalang.util.codegen.ProgramFile;
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
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        ProgramFile[] programFiles = Arrays.stream(sourceFilePaths).map(BTestRunner::buildTestModel)
                .toArray(ProgramFile[]::new);
        Arrays.stream(programFiles).forEachOrdered(programFile -> {
            TesterinaRegistry.getInstance().addProgramFile(programFile);
        });

        if (ModeResolver.getInstance().isDebugEnabled()) {
            DebugManager debugManager = DebugManager.getInstance();
            // This will start the websocket server.
            debugManager.init();
        }

        executeTestFunctions(programFiles);
        tReport.printTestSummary();
        Runtime.getRuntime().exit(0);
    }

    private static ProgramFile buildTestModel(Path sourceFilePath) {
        return new BLangProgramLoader().loadServiceProgramFile(programDirPath, sourceFilePath);
    }

    private static void executeTestFunctions(ProgramFile[] programFiles) {
        TesterinaContext tFile = new TesterinaContext(programFiles);
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
                        "error in '" + tFunction.getName() + "': " + e.getMessage());
            }
        }

        //test
        for (TesterinaFunction tFunction : testFunctions) {
            boolean isTestPassed = true;
            String errorMessage = null;
            try {
                tFunction.invoke();
            } catch (BallerinaException e) {
                isTestPassed = false;
                errorMessage = e.getMessage();
                outStream.println("test '" + tFunction.getName() + "' failed: " + errorMessage);
            } catch (Exception e) {
                isTestPassed = false;
                errorMessage = e.getMessage();
                outStream.println("test '" + tFunction.getName() + "' has an error: " + errorMessage);
            }
            // if there are no exception thrown, test is passed
            TesterinaResult functionResult = new TesterinaResult(tFunction.getName(), isTestPassed,
                    errorMessage);
            tReport.addFunctionResult(functionResult);
        }

        //after test
        for (TesterinaFunction tFunction : afterTestFunctions) {
            try {
                tFunction.invoke();
            } catch (Exception e) {
                outStream.println("error in '" + tFunction.getName() + "': " + e.getMessage());
            }
        }
    }

}
