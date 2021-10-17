/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugger.test.adapter;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

/**
 * Test class for debugger output related scenarios.
 */
public class DebugOutputTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "debug-output-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Debug output test for Ballerina programs with runtime errors")
    public void testProgramOutput() throws BallerinaTestException {
        Path filePath = debugTestRunner.testEntryFilePath;
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 19));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));
        List<Pair<String, OutputEventArguments>> outputs = debugTestRunner.waitForDebugOutputs(1000);

        // Validates initial debug outputs which uses `CONSOLE` category.
        Assert.assertEquals(outputs.get(0).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(0).getLeft(), "Waiting for debug process to start..." +
                System.lineSeparator() + System.lineSeparator());

        Assert.assertEquals(outputs.get(1).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(1).getLeft(), "Compiling source" +
                System.lineSeparator());

        Assert.assertEquals(outputs.get(2).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(2).getLeft().trim(), "debug_test_resources/debug_output_test:0.0.1");

        Assert.assertEquals(outputs.get(4).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(4).getLeft(), "Running executable" + System.lineSeparator());

        // Resumes the program and waits for the program termination.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        boolean terminated = debugTestRunner.waitForDebugTermination(10000);
        Assert.assertTrue(terminated);
        outputs = debugTestRunner.waitForDebugOutputs(1000);

        // Validates debugger outputs with Ballerina runtime errors, which uses `STDERR` category.
        Assert.assertEquals(outputs.get(0).getRight().getCategory(), OutputEventArgumentsCategory.STDERR);
        Assert.assertEquals(outputs.get(0).getLeft().trim(), "error: {ballerina}DivisionByZero " +
                "{\"message\":\" / by zero\"}");
        Assert.assertEquals(outputs.get(1).getRight().getCategory(), OutputEventArgumentsCategory.STDERR);
        Assert.assertEquals(outputs.get(1).getLeft().trim(), "at debug_test_resources.debug_output_test." +
                "0:main(main.bal:19)");
        Assert.assertEquals(outputs.get(2).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertTrue(outputs.get(2).getLeft().trim().startsWith("Disconnected from the target VM, address: " +
                "'localhost:"));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
