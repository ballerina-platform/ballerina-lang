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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test class for debugger output related scenarios.
 */
public class DebugOutputTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @Override
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
        Assert.assertEquals(outputs.get(0).getLeft(), "Waiting for the debug process to start..." +
                System.lineSeparator() + System.lineSeparator());

        Assert.assertEquals(outputs.get(1).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(1).getLeft(), "Compiling source" +
                System.lineSeparator());

        Assert.assertEquals(outputs.get(2).getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
        Assert.assertEquals(outputs.get(2).getLeft().trim(), "debug_test_resources/debug_output_test:0.0.1");

        Optional<Pair<String, OutputEventArguments>> runningExecOutput = outputs.stream()
                .filter(o -> o.getLeft().equals("Running executable" + System.lineSeparator()))
                .findFirst();
        Assert.assertTrue(runningExecOutput.isPresent(), "Expected 'Running executable' output not found");
        Assert.assertEquals(runningExecOutput.get().getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);

        // Validates that the debugger does not report the current JRE as incompatible.
        Assert.assertTrue(outputs.stream().noneMatch(o -> o.getLeft().contains("Incompatible JRE version")),
                "Unexpected incompatible JRE version warning found in debug outputs");

        // Resumes the program and waits for the program termination.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        boolean terminated = debugTestRunner.waitForDebugTermination(10000);
        Assert.assertTrue(terminated);

        // The program error outputs and the VM disconnection output can be received after the termination event.
        // Therefore, collects the outputs until the VM disconnection output is received.
        List<Pair<String, OutputEventArguments>> terminationOutputs = new ArrayList<>();
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline && terminationOutputs.stream()
                .noneMatch(o -> o.getLeft().contains("Disconnected from the target VM"))) {
            try {
                terminationOutputs.addAll(debugTestRunner.waitForDebugOutputs(1000));
            } catch (BallerinaTestException ignored) {
                // Continues until the deadline.
            }
        }

        // Validates debugger outputs with Ballerina runtime errors, which uses `STDERR` category.
        int errorIndex = findOutputIndex(terminationOutputs, "error: {ballerina}DivisionByZero " +
                "{\"message\":\" / by zero\"}");
        Assert.assertEquals(terminationOutputs.get(errorIndex).getRight().getCategory(),
                OutputEventArgumentsCategory.STDERR);
        int stackTraceIndex = findOutputIndex(terminationOutputs, "at debug_test_resources.debug_output_test." +
                "0:main(main.bal:19)");
        Assert.assertEquals(terminationOutputs.get(stackTraceIndex).getRight().getCategory(),
                OutputEventArgumentsCategory.STDERR);
        Assert.assertTrue(errorIndex < stackTraceIndex, "Expected the error output before the stack trace");

        Optional<Pair<String, OutputEventArguments>> disconnectOutput = terminationOutputs.stream()
                .filter(o -> o.getLeft().trim().startsWith("Disconnected from the target VM, address: 'localhost:"))
                .findFirst();
        Assert.assertTrue(disconnectOutput.isPresent(), "Expected VM disconnection output not found");
        Assert.assertEquals(disconnectOutput.get().getRight().getCategory(), OutputEventArgumentsCategory.CONSOLE);
    }

    private static int findOutputIndex(List<Pair<String, OutputEventArguments>> outputs, String expectedOutput) {
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).getLeft().trim().equals(expectedOutput)) {
                return i;
            }
        }
        Assert.fail("Expected output not found: " + expectedOutput);
        return -1;
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
