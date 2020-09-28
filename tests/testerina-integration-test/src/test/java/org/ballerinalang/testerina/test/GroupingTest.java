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
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test class containing tests related to test grouping.
 */
public class GroupingTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFilesProjectPath.resolve("grouping").toString();
    }

    @Test
    public void testSingleGroupExecution() throws BallerinaTestException {
        String msg = "3 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--groups", "g1", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testMultipleGroupExecution() throws BallerinaTestException {
        String msg = "3 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--groups", "g2,g4", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testSingleGroupExclusion() throws BallerinaTestException {
        String msg1 = "4 passing";
        String msg2 = "1 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        balClient.runMain("test", new String[]{"--disable-groups", "g5", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(50000);
        clientLeecher2.waitForText(50000);
    }

    @Test
    public void testMultipleGroupExclusion() throws BallerinaTestException {
        String msg = "1 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--disable-groups", "g1,g5,g6", "groups-test.bal"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testNonExistingGroupInclusion() throws BallerinaTestException {
        String msg = "No tests found";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--groups", "g10", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testNonExistingGroupExclusion() throws BallerinaTestException {
        String msg1 = "4 passing";
        String msg2 = "2 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        balClient.runMain("test", new String[]{"--disable-groups", "g10", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(80000);
        clientLeecher2.waitForText(80000);
    }

    @Test
    public void testListingOfTestGroups() throws BallerinaTestException {
        String msg = "[g1, g2, g3, g4, g5, g6]";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--list-groups", "groups-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void beforeGroupsAfterGroups1() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"before-groups-after-groups-test.bal"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("[fail] afterSuiteFunc")) {
            throw new BallerinaTestException("Test failed due to assertion failure in after suite function");
        }
    }

    @Test
    public void beforeGroupsAfterGroups2() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test",
                new String[]{"before-groups-after-groups-test2.bal"}, new HashMap<>(), projectPath, true);
        if (errorOutput.contains("[fail] afterSuiteFunc")) {
            throw new BallerinaTestException("Test failed due to assertion failure in after suite function");
        }
    }

}
