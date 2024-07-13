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
import org.testng.annotations.DataProvider;
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
        FileUtils.copyFolder(Paths.get("build/libs"), Paths.get(projectPath, "runtime-api-tests", "libs"));
    }

    @Test(dataProvider = "basic-cases-provider")
    public void testAssertions(String[] args, String expectedOutputPath) throws BallerinaTestException, IOException {
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput(expectedOutputPath, output);
    }

    @DataProvider(name = "basic-cases-provider")
    public Object[][] basicCasesDataProvider() {
        return new Object[][]{
                {mergeCoverageArgs("assertions"), "BasicCasesTest-testAssertions.txt"},
                {mergeCoverageArgs("assertions-diff-error"), "BasicCasesTest-testAssertDiffError.txt"},
                {mergeCoverageArgs("assertions-error-messages"), "BasicCasesTest-testAssertionErrorMessage.txt"},
                {mergeCoverageArgs("assertions-behavioral-types"), "BasicCasesTest-testAssertBehavioralTypes.txt"},
                {mergeCoverageArgs("assertions-structural-types"), "BasicCasesTest-testAssertStructuralTypes.txt"},
                {mergeCoverageArgs("assertions-sequence-types"), "BasicCasesTest-testAssertSequenceTypes.txt"},
                {mergeCoverageArgs("interops"), "BasicCasesTest-testJavaInterops.txt"},
                {mergeCoverageArgs("runtime-api-tests"), "BasicCasesTest-testRuntimeApi.txt"},
                {mergeCoverageArgs("before-after"), "BasicCasesTest-testBeforeAfter.txt"},
                {mergeCoverageArgs("before-each-after-each"), "BasicCasesTest-testBeforeEachAfterEach.txt"},
                {mergeCoverageArgs("depends-on"), "BasicCasesTest-testDependsOn.txt"},
                {mergeCoverageArgs("annotations"), "BasicCasesTest-testAnnotations.txt"},
                {mergeCoverageArgs("isolated-functions"), "BasicCasesTest-testIsolatedFunctions.txt"},
                {mergeCoverageArgs("intersection-type-test"), "BasicCasesTest-testIntersectionTypes.txt"},
                {mergeCoverageArgs("anydata-type-test"), "BasicCasesTest-testAnydataType.txt"},
                {mergeCoverageArgs("async"), "BasicCasesTest-testAsyncInvocation.txt"},
        };
    }

    @Test
    public void testAnnotationAccess() throws BallerinaTestException, IOException {
        String endString = " SEVERE {b7a.log.crash} - ";
        String firstString = "We thank you for helping make us better.";
        String endString2 = "********";
        String firstString2 = "unnamed module of loader 'app')";
        String[] args = mergeCoverageArgs("annotation-access");
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, true);
        output = output + "********";
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        output = CommonUtils.replaceVaryingString(firstString2, endString2, output);
        AssertionUtils.assertOutput("BasicCasesTest-testAnnotationAccess.txt", output);
    }
}
