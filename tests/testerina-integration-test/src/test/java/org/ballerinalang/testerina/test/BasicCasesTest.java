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
    public void testAssertions() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion failure");
    }

    @Test
    public void testAssertDiffError() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-diff-error"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion diff message failure in test framework");
    }

    @Test
    public void testAssertionErrorMessage() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-error-messages"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion diff message failure");
    }

    @Test
    public void testAssertBehavioralTypes() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-behavioral-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion failure for behavioral data types");
    }

    @Test
    public void testAssertStructuralTypes() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-structural-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion failure for structural data types");
    }

    @Test
    public void testAssertSequenceTypes() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"assertions-sequence-types"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "assertion failure for sequence data types");
    }

    @Test
    public void testAnnotationAccess() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"annotation-access"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "test annotation access failure");
    }

    @Test
    public void testJavaInterops() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"interops"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "interops failure");
    }

    @Test
    public void testRuntimeApi() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"runtime-api-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "runtime api failure");
    }

    @Test
    public void testBeforeAfter() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"before-after"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "before-after annotation attribute failure");
    }

    @Test
    public void testBeforeEachAfterEach() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"before-each-after-each"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "before-each-after-each annotation attribute failure");
    }

    @Test(dependsOnMethods = "testBeforeAfter")
    public void testDependsOn() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"depends-on"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "depends-on annotation attribute failure");
    }

    @Test(dependsOnMethods = "testDependsOn")
    public void testAnnotations() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"annotations"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "annotations failure");
    }

    @Test
    public void testIsolatedFunctions() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"isolated-functions"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "isolated functions failure");
    }

    @Test
    public void testIntersectionTypes() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"intersection-type-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "intersection type failure");
    }

    @Test
    public void testAnydataType() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"anydata-type-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "anydata type failure");
    }

    @Test
    public void testAsyncInvocation() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"async"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "async invocation failure");
    }
}
