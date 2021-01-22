/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Test class containing tests related to selective function tests.
 */
public class SelectedFunctionTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("single-test-execution").toString();
    }

    @Test
    public void testSingleFunctionExecution() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test",
                new String[]{"--tests", "testFunc", "single-test-execution.bal"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(errorOutput, "single function execution failure");
    }

    @Test
    public void testDependentFunctionExecution() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test",
                new String[]{"--tests", "testFunc2", "single-test-execution.bal"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(errorOutput, "dependant function execution failure");
    }

    @Test
    public void testMultipleFunctionExecution() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test",
                new String[]{"--tests", "testFunc,testFunc2", "single-test-execution.bal"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(errorOutput, "multiple function execution failure");
    }

    @Test
    public void testNonExistingFunctionExecution() throws BallerinaTestException {
        String msg = "No tests found";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "nonExistingFunc", "single-test-execution.bal"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testDisabledFunctionExecution() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test",
                new String[]{"--tests", "testDisabledFunc", "single-test-execution.bal"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(errorOutput, "disabled function execution failure");
    }

    @Test
    public void testDependentDisabledFunctionExecution() throws BallerinaTestException {
        String errMsg = "error: Test [testDependentDisabledFunc] depends on function [testDisabledFunc], " +
                "but it is either disabled or not included.";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"--tests", "testDependentDisabledFunc",
                        "single-test-execution.bal"}, null, new String[]{}, new LogLeecher[]{clientLeecher},
                projectPath);
        clientLeecher.waitForText(20000);
    }
}
