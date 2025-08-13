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
import org.ballerinalang.testerina.test.utils.CommonUtils;
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
    public void setup() {
        balClient = new BMainInstance(balServer);
    }

    @Test
    public void testMockingNonExistingFunction() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingNonExistingFunction.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingNonExistingFunction() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-function-legacy-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingNonExistingFunction.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testMockingWithoutAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("record-less-annotation-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingWithoutAnnotationRecord.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingWithoutAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("record-less-annotation-function-legacy-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingWithoutAnnotationRecord.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testMockingWithEmptyAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("empty-annotation-record-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingWithEmptyAnnotationRecord.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingWithEmptyAnnotationRecord() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("empty-annotation-record-function-legacy-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingWithEmptyAnnotationRecord.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testMockingFunctionInNonExistingModule() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-module-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingFunctionInNonExistingModule.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingFunctionInNonExistingModule() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-module-legacy-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingFunctionInNonExistingModule.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testMockingFunctionWithIncompatibleTypes() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("incompatible-type-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, false);
        String firstString = "tests.test_execute-generated_";
        String endString = "lineNumber";
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingFunctionWithIncompatibleTypes.txt",
                output.replaceAll("(?m)^[ \t]*\r?\n", "\n").replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingFunctionWithIncompatibleTypes() throws BallerinaTestException, IOException {
        String projectPath = projectBasedTestsPath.resolve("incompatible-type-legacy-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingFunctionWithIncompatibleTypes.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testMockingFunctionInSingleFileProject() throws BallerinaTestException, IOException {
        String projectPath = singleFileTestsPath.resolve("mocking").toString();
        String[] args = mergeCoverageArgs(new String[]{"function-mock.bal"});
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testMockingFunctionInSingleFileProject.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }

    @Test
    public void testLegacyMockingFunctionInSingleFileProject() throws BallerinaTestException, IOException {
        String projectPath = singleFileTestsPath.resolve("mocking").toString();
        String[] args = mergeCoverageArgs(new String[]{"function-legacy-mock.bal"});
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(projectPath,
                "InvalidFunctionMockingTestCase-testLegacyMockingFunctionInSingleFileProject.txt",
                output.replaceAll("\r\n|\r|\n", "\n"));
    }
}
