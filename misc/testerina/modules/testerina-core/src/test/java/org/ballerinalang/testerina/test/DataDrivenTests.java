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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests to cover data set based testing.
 */
public class DataDrivenTests {

    private String sourceRoot = "src/test/resources/annotations-test";
    private Path[] filePaths = { Paths.get("data-driven-positive-test.bal") };
    private Path[] filePathsGrouped = {
            Paths.get("data-driven-with-groups-test.bal") };

    @Test
    public void dataSetTest() {
        BTestRunner testRunner = new BTestRunner();
        testRunner.runTest(sourceRoot, filePaths, null);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 2);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 2);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 0);
    }

    @Test
    public void dataSetWithGroupFilterTest() {
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g1");
        testRunner.runTest(sourceRoot, filePathsGrouped, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 2);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 2);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 4);
    }

    @Test
    public void dataSetWithMultipleGroupFilterTest() {
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("g1");
        groupList.add("g2");
        groupList.add("default");
        testRunner.runTest(sourceRoot, filePathsGrouped, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 6);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 5);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 1);
    }

    @Test
    public void dataSetWithDefaultGroupFilterTest() {
        BTestRunner testRunner = new BTestRunner();
        List<String> groupList = new ArrayList<>();
        groupList.add("default");
        testRunner.runTest(sourceRoot, filePathsGrouped, groupList);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "passed"), 1);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "failed"), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getTestSummary(".", "skipped"), 4);
    }
}
