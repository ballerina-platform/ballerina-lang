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
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test class containing tests related to selective function tests.
 */
public class SelectedFunctionTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("single-test-execution").toString();
    }

    @Test
    public void testSingleFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunc", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("SelectedFunctionTest-testSingleFunctionExecution.txt", output);
    }

    @Test
    public void testDependentFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunc2", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("SelectedFunctionTest-testDependentFunctionExecution.txt", output);
    }

    @Test
    public void testMultipleFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunc,testFunc2", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("SelectedFunctionTest-testMultipleFunctionExecution.txt", output);
    }

    @Test
    public void testNonExistingFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "nonExistingFunc", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SelectedFunctionTest-testNonExistingFunctionExecution.txt", output);
    }

    @Test
    public void testDisabledFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testDisabledFunc", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("SelectedFunctionTest-testDisabledFunctionExecution.txt", output);
    }

    @Test
    public void testDependentDisabledFunctionExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(
                new String[]{"--tests", "testDependentDisabledFunc", "single-test-execution.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SelectedFunctionTest-testDependentDisabledFunctionExecution.txt",
                output);
    }
}
