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
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
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
        projectPath = singleFileTestsPath.resolve("grouping").toString();
    }

    @Test
    public void testSingleGroupExecution() throws BallerinaTestException, IOException {
        String msg = "3 passing";
        String[] args = mergeCoverageArgs(new String[]{"--groups", "g1", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testSingleGroupExecution.txt", output);
    }

    @Test
    public void testMultipleGroupExecution() throws BallerinaTestException, IOException {
        String msg = "3 passing";
        String[] args = mergeCoverageArgs(new String[]{"--groups", "g2,g4", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testMultipleGroupExecution.txt", output);
    }

    @Test
    public void testSingleGroupExclusion() throws BallerinaTestException, IOException {
        String msg1 = "4 passing";
        String msg2 = "1 failing";
        String[] args = mergeCoverageArgs(new String[]{"--disable-groups", "g5", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testSingleGroupExclusion.txt", output);
    }

    @Test
    public void testMultipleGroupExclusion() throws BallerinaTestException, IOException {
        String msg = "1 passing";
        String[] args = mergeCoverageArgs(new String[]{"--disable-groups", "g1,g5,g6", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testMultipleGroupExclusion.txt", output);
    }

    @Test
    public void testNonExistingGroupInclusion() throws BallerinaTestException, IOException {
        String msg = "No tests found";
        String[] args = mergeCoverageArgs(new String[]{"--groups", "g10", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testNonExistingGroupInclusion.txt", output);
    }

    @Test
    public void testNonExistingGroupExclusion() throws BallerinaTestException, IOException {
        String msg1 = "4 passing";
        String msg2 = "2 failing";
        String[] args = mergeCoverageArgs(new String[]{"--disable-groups", "g10", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testNonExistingGroupExclusion.txt", output);
    }

    @Test
    public void testListingOfSingleTestGroups() throws BallerinaTestException, IOException {
        String msg = "[g1, g2, g3, g4, g5, g6]";
        String[] args = mergeCoverageArgs(new String[]{"--list-groups", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testListingOfSingleTestGroups.txt", output);
    }

    @Test
    public void testListingOfTestGroups() throws BallerinaTestException, IOException {
        String msg = "[g1, g2, g3, g4, g5, g6]";
        String[] args = {"--list-groups"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(),
                projectBasedTestsPath.resolve("group-test").toString(),
                false);
        AssertionUtils.assertOutput("GroupingTest-testListingOfTestGroups.txt", output);
    }

    @Test
    public void testListGroupsWithOtherFlags() throws BallerinaTestException, IOException {
        String msg = "Warning: Other flags are skipped when list-groups flag is provided.";
        String[] args = mergeCoverageArgs(new String[]{"--groups", "g1", "--list-groups", "groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testListGroupsWithOtherFlags.txt", output);
    }

    /**
     * Test before and after groups functions with other test configurations when there is a single group.
     *
     * @throws BallerinaTestException
     */
    @Test
    public void beforeGroupsAfterGroups1() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"before-groups-after-groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("GroupingTest-beforeGroupsAfterGroups1.txt", output);
    }

    /**
     * Test before and after groups functions with other test configurations when there are multiple groups.
     *
     * @throws BallerinaTestException
     */
    @Test
    public void beforeGroupsAfterGroups2() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"before-groups-after-groups-test2.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("GroupingTest-beforeGroupsAfterGroups2.txt", output);
    }

    @Test
    public void afterGroupsWithDisabledTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--groups", "g1", "after-groups-with-disabled-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("GroupingTest-afterGroupsWithDisabledTest.txt", output);
    }

    @Test
    public void failedBeforeGroupTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"failed-before-groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("GroupingTest-failedBeforeGroupTest.txt", output);
    }

    @Test
    public void failedBeforeEachTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"failed-before-each-with-groups.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("GroupingTest-failedBeforeEachTest.txt", output);
    }

    @Test
    public void testWhenAfterGroupsFails() throws BallerinaTestException, IOException {
        String msg1 = "5 passing";
        String msg2 = "0 failing";
        String msg3 = "0 skipped";
        String[] args = mergeCoverageArgs(new String[]{"failed-after-groups-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("GroupingTest-testWhenAfterGroupsFails.txt", output);
    }

}
