/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.ballerinalang.testerina.test;

import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test class to test grouping.
 */
public class GroupFilterTest {

    private String sourceRoot = "src/test/resources/annotations-test";
    private Path[] filePaths = {Paths.get("groups-test.bal")};

    @Test
    public void singleGroupFilterTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g1");
        testRunner.runTest(sourceRoot, filePaths, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 3);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 0);
        // disabled tests won't be counted as skipped
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    @Test
    public void multipleGroupFilterTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g2");
        groupList.add("g4");
        testRunner.runTest(sourceRoot, filePaths, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 3);
    }

    @Test
    public void specifyNonExistingGroupTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g10");
        testRunner.runTest(sourceRoot, filePaths, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    @Test
    public void groupFilterWithFailuresTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g5");
        try {
            testRunner.runTest(sourceRoot, filePaths, groupList);
        } catch (RuntimeException e) {
        }
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 1);
    }

    @Test
    public void noGroupFiltersTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        testRunner.runTest(sourceRoot, filePaths, null);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 4);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 1);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    // Tests group exclude filters
    @Test
    public void excludeSingleGroupTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g5");
        testRunner.runTest(sourceRoot, filePaths, groupList, false);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 4);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    @Test
    public void excludeMultipleGroupsTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g4");
        groupList.add("g5");
        testRunner.runTest(sourceRoot, filePaths, groupList, false);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 3);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    @Test
    public void excludeNonExistingGroupTest() {
        cleanup();
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g10");
        testRunner.runTest(sourceRoot, filePaths, groupList, false);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 4);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 1);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }
}
