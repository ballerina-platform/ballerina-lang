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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test class to test Module test execution.
 */
public class ModuleExecutionTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("module-execution-tests").toString();
    }

    @Test()
    public void test_DefaultModule_AllTests() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("test",
                new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution:*"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "default module test failure");
    }

    @Test()
    public void test_DefaultModule_SingleTest() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "[pass] main_test1";
        String[] args = new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution:main_test1"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            throw new BallerinaTestException("Test failed due to default module single test failure.");
        }
    }

    @Test()
    public void test_DefaultModule_StartWildCardTest() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "[pass] commonTest";
        String[] args = new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution:*Test"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            throw new BallerinaTestException("Test failed due to default module wild card test failure.");
        }
    }

    @Test()
    public void test_DefaultModule_MiddleWildCardTest() throws BallerinaTestException {
        String msg1 = "3 passing";
        String msg2 = "[pass] main_test1";
        String msg3 = "[pass] main_test2";
        String msg4 = "[pass] main_test3";
        String[] args = new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution:*test*"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3) || !output.contains(msg4)) {
            throw new BallerinaTestException("Test failed due to default module wild card test failure.");
        }
    }

    @Test()
    public void test_DefaultModule_EndWildCardTest() throws BallerinaTestException {
        String msg1 = "3 passing";
        String msg2 = "[pass] main_test1";
        String msg3 = "[pass] main_test2";
        String msg4 = "[pass] main_test3";
        String[] args = new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution:main_*"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3) || !output.contains(msg4)) {
            throw new BallerinaTestException("Test failed due to default module wild card test failure.");
        }
    }

    @Test()
    public void test_Module1_AllTests() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("test",
                new String[]{"--code-coverage", "--includes=*", "--tests", "moduleExecution.Module1:*"},
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "module wise test failure");
    }

    @Test()
    public void test_Module1_SingleTest() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "[pass] module1_test1";
        String[] args = new String[]{"--code-coverage", "--includes=*", "--tests",
                "moduleExecution.Module1:module1_test1"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            throw new BallerinaTestException("Test failed due to module wise single test failure.");
        }
    }

    @Test()
    public void test_Module1_WildCardTest() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "[pass] module1_test1";
        String msg3 = "[pass] module1_test2";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "moduleExecution.Module1:module1_*"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            throw new BallerinaTestException("Test failed due to module wise wild card test failure.");
        }
    }

    @Test()
    public void test_WildCardTest() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "[pass] commonTest_Module1";
        String msg3 = "[pass] commonTest";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "common*"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            throw new BallerinaTestException("Test failed due to wild card test failure.");
        }
    }

    @Test()
    public void test_Module1_WithGroups() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "[pass] module1_test2";

        String[] args = mergeCoverageArgs(new String[]{"--tests", "moduleExecution.Module1:*", "--groups", "g1"});
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, false);

        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to module with groups failure.");
        }
    }

    @AfterMethod
    public void copyExec() {
        try {
            FileUtils.copyBallerinaExec(Paths.get(projectPath), String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            // ignore exception
        }
    }
}
