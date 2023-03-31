/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.ballerinalang.testerina.test.utils.CommonUtils;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class BasicCasesTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass()
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
        FileUtils.copyFolder(Paths.get("build/libs"),
                Paths.get(projectPath, "runtime-api-tests", "libs"));
    }

    @Test
    public void testAssertions() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertions.txt", output);
    }

    @Test
    public void testAssertDiffError() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-diff-error"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertDiffError.txt", output);
    }

    @Test
    public void testAssertionErrorMessage() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-error-messages"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertionErrorMessage.txt", output);
    }

    @Test
    public void testAssertBehavioralTypes() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-behavioral-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertBehavioralTypes.txt", output);
    }

    @Test
    public void testAssertStructuralTypes() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-structural-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertStructuralTypes.txt", output);
    }

    @Test
    public void testAssertSequenceTypes() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-sequence-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAssertSequenceTypes.txt", output);
    }

    @Test
    public void testAnnotationAccess() throws BallerinaTestException, IOException {
        String endString = " SEVERE {b7a.log.crash} - ";
        String firstString = "We thank you for helping make us better.";
        String[] args = mergeCoverageArgs(new String[]{"annotation-access"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        AssertionUtils.assertOutput("BasicCasesTest-testAnnotationAccess.txt", output);
    }

    @Test
    public void testJavaInterops() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"interops"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testJavaInterops.txt", output);
    }

    @Test
    public void testRuntimeApi() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"runtime-api-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testRuntimeApi.txt", output);
    }

    @Test
    public void testBeforeAfter() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"before-after"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testBeforeAfter.txt", output);
    }

    @Test
    public void testBeforeEachAfterEach() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"before-each-after-each"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testBeforeEachAfterEach.txt", output);
    }

    @Test(dependsOnMethods = "testBeforeAfter")
    public void testDependsOn() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"depends-on"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testDependsOn.txt", output);
    }

    @Test(dependsOnMethods = "testDependsOn")
    public void testAnnotations() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"annotations"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAnnotations.txt", output);
    }

    @Test
    public void testIsolatedFunctions() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"isolated-functions"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testIsolatedFunctions.txt", output);
    }

    @Test
    public void testIntersectionTypes() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"intersection-type-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testIntersectionTypes.txt", output);
    }

    @Test
    public void testAnydataType() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"anydata-type-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAnydataType.txt", output);
    }

    @Test
    public void testAsyncInvocation() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"async"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("BasicCasesTest-testAsyncInvocation.txt", output);
    }
}
