/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

/**
 * Test cases for skipping scenarios
 */
public class SkipTest {

    private String sourceRoot = "src/test/resources/skip-tests";

    @Test
    public void tesSkipWhenDependsOnFunctionFails() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(sourceRoot, new Path[]{Paths.get("dependson-skip-test.bal")}, new
            ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "skipped"), 2);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 2);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "failed"), 2);
    }

    @Test
    public void testSkipWhenBeforeFails() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(sourceRoot, new Path[]{Paths.get("skip-when-before-fails.bal")}, new
            ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "skipped"), 1);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 1);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "failed"), 1);
    }

    @Test
    public void testSkipDependentsWhenBeforeFails() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(sourceRoot, new Path[]{Paths.get("skip-dependents-when-before-fails.bal")}, new
            ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "skipped"), 2);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 1);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "failed"), 1);
    }

    @Test
    public void testSkipWhenBeforeSuiteFails() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(sourceRoot, new Path[]{Paths.get("skip-when-beforeSuite-fails.bal")}, new
            ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "skipped"), 3);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 0);
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "failed"), 0);
    }

    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }
}
