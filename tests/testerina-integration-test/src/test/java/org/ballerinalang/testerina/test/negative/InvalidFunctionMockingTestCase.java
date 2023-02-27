/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.io.IOException;
import java.util.HashMap;



/**
 * Negative test cases for function mocking.
 */
public class InvalidFunctionMockingTestCase extends BaseTestCase {
    private BMainInstance balClient;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    @Test
    public void testMockingNonExistingFunction() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-testMockingNonExistingFunction.txt",
                output);
    }

    @Test
    public void testMockingNonExistingFunction2() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-function-mock2").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-testMockingNonExistingFunction2.txt",
                output);
    }

    @Test
    public void testMockingWithoutAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("record-less-annotation-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                        "testMockingWithoutAnnotationRecord.txt", output);
    }

    @Test
    public void testMockingWithoutAnnotationRecord2() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("record-less-annotation-function-mock2").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingWithoutAnnotationRecord2.txt", output);
    }

    @Test
    public void testMockingWithEmptyAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("empty-annotation-record-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingWithEmptyAnnotationRecord.txt", output);
    }

    @Test
    public void testMockingWithEmptyAnnotationRecord2() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("empty-annotation-record-function-mock2").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingWithEmptyAnnotationRecord2.txt", output);
    }

    @Test
    public void testMockingFunctionInNonExistingModule() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-module-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingFunctionInNonExistingModule.txt", output);
    }

    @Test
    public void testMockingFunctionInNonExistingModule2() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-module-mock2").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingFunctionInNonExistingModule2.txt", output);
    }

    @Test
    public void testMockingFunctionWithIncompatibleTypes() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("incompatible-type-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingFunctionWithIncompatibleTypes.txt", output);
    }

    @Test
    public void testMockingFunctionInSingleFileProject() throws BallerinaTestException, IOException {
        String projectPath = singleFileTestsPath.resolve("mocking").toString();
        String[] args = mergeCoverageArgs(new String[]{"function-mock.bal"});
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("InvalidFunctionMockingTestCase-" +
                "testMockingFunctionInSingleFileProject.txt", output);
    }
}
