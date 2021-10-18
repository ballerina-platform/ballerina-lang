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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    public void testSkipWhenDependsOnFunctionFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "1 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"dependson-skip-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when using dependsOn.");
        }
    }

    @Test
    public void testSkipWhenBeforeFails() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-before-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when before test fails.");
        }
    }

    @Test
    public void testSkipWhenAfterFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "1 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-after-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when after test fails.");
        }
    }

    @Test
    public void testSkipWhenBeforeEachFails() throws BallerinaTestException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeEach-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when before " +
                    "each test fails.");
        }
    }


    @Test
    public void testSkipWhenAfterEachFails() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-afterEach-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when after each fails.");
        }
    }

    @Test
    public void testSkipWhenBeforeSuiteFails() throws BallerinaTestException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeSuite-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when before suite fails.");
        }
    }

    @Test
    public void testSkipWhenBeforeGroupsFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        String[] args = mergeCoverageArgs(new String[]{"skip-when-beforeGroups-fails.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to error while skipping test when before groups fails.");
        }
    }

}
