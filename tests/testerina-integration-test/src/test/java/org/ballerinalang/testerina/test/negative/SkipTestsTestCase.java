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
 * Negative test cases for missing before,after and dependsOn functions.
 */
public class SkipTestsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("skip-tests").toString();
    }

    @Test
    public void testSkipWhenDependsOnFunctionFails() throws BallerinaTestException, IOException {
        String msg1 = "2 passing";
        String msg2 = "1 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"dependson-skip-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenDependsOnFunctionFails.txt", output);
    }

    @Test
    public void testSkipWhenBeforeFails() throws BallerinaTestException, IOException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-before-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenBeforeFails.txt", output);
    }

    @Test
    public void testSkipWhenAfterFails() throws BallerinaTestException, IOException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "1 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-after-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenAfterFails.txt", output);
    }

    @Test
    public void testSkipWhenBeforeEachFails() throws BallerinaTestException, IOException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeEach-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenBeforeEachFails.txt", output);
    }


    @Test
    public void testSkipWhenAfterEachFails() throws BallerinaTestException, IOException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-afterEach-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenAfterEachFails.txt", output);
    }

    @Test
    public void testSkipWhenBeforeSuiteFails() throws BallerinaTestException, IOException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeSuite-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenBeforeSuiteFails.txt", output);
    }

    @Test
    public void testSkipWhenBeforeGroupsFails() throws BallerinaTestException, IOException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeGroups-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("SkipTestsTestCase-testSkipWhenBeforeGroupsFails.txt", output);
    }

}
