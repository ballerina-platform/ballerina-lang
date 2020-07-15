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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        projectPath = singleFilesProjectPath.resolve("single-test-execution").toString();
    }

    @Test
    public void testSingleFunctionExecution() throws BallerinaTestException {
        String msg = "1 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "testFunc", "single-test-execution.bal"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testDependentFunctionExecution() throws BallerinaTestException {
        String msg = "2 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "testFunc2", "single-test-execution.bal"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testMultipleFunctionExecution() throws BallerinaTestException {
        String msg = "2 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "testFunc,testFunc2",
                "single-test-execution.bal"}, null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testNonExistingFunctionExecution() throws BallerinaTestException {
        String msg = "No tests found with the given name/s";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "nonExistingFunc", "single-test-execution.bal"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testDisabledFunctionExecution() throws BallerinaTestException {
        String msg = "No tests found with the given name/s";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--tests", "testDisabledFunc", "single-test-execution.bal"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testDependentDisabledFunctionExecution() throws BallerinaTestException {
        String errMsg = "error: Test [testDependentDisabledFunc] depends on function [testDisabledFunc], " +
                "but it couldn't be found.";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"--tests", "testDependentDisabledFunc",
                        "single-test-execution.bal"}, null, new String[]{}, new LogLeecher[]{clientLeecher},
                projectPath);
        clientLeecher.waitForText(20000);
    }
}
