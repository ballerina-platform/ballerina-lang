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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class containting tests related to Rerun failed test functionality.
 */
public class RerunFailedTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
    }

    @Test
    public void testFullTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"rerun-failed-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        String firstString = "tests.test_execute-generated_";
        String endString = "lineNumber";
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        AssertionUtils.assertOutput("RerunFailedTest-testFullTest.txt", output.replaceAll("\r", ""));
    }

    @Test (dependsOnMethods = "testFullTest")
    public void testRerunFailedTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--rerun-failed", "rerun-failed-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        String firstString = "tests.test_execute-generated_";
        String endString = "lineNumber";
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        AssertionUtils.assertOutput("RerunFailedTest-testRerunFailedTest.txt", output.replaceAll("\r", ""));
    }

    @Test (dependsOnMethods = "testRerunFailedTest")
    public void testRerunFailedTestWithoutAnInitialRun() throws BallerinaTestException, IOException {
        // delete the target directory along with rerun_test.json file
        String packageDirName = "rerun-failed-tests";
        runBalClean(packageDirName);

        String[] args = new String[]{"--rerun-failed", packageDirName};
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("RerunFailedTest-testRerunFailedTestWithoutAnInitialRun.txt",
                output.replaceAll("\r", ""));
    }

    @Test
    public void testRerunFailedTestWithInvalidRunTestJson() throws BallerinaTestException, IOException {
        String[] args = new String[]{"--rerun-failed", "rerun-failed-tests-with-invalid-json"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("RerunFailedTest-testRerunFailedTestWithInvalidRunTestJson.txt",
                output.replaceAll("\r", ""));
    }

    @Test
    public void testRerunFailedTestWithMissingModuleNameInRunTestJson() throws BallerinaTestException, IOException {
        String[] args = new String[]{"--rerun-failed", "rerun-failed-tests-with-missing-module-name"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("RerunFailedTest-testRerunFailedTestWithMissingModuleNameInRunTestJson.txt",
                output.replaceAll("\r", ""));
    }

    @AfterMethod
    public void copyExec() {
        try {
            FileUtils.copyBallerinaExec(Path.of(projectPath), String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            // ignore exception
        }
    }

    private void runBalClean(String packageDirName) throws BallerinaTestException {
        String[] args = new String[]{"--target-dir", packageDirName + "/target"};
        Map<String, String> envProperties = new HashMap<>();
        envProperties.put("user.dir", projectPath);
        balClient.runMain("clean", args, envProperties, null, null, projectPath);
    }
}
