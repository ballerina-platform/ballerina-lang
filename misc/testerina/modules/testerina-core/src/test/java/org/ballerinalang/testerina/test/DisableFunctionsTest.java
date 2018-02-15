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

/**
 * Tests disabling of test functions.
 */
public class DisableFunctionsTest {

    private Path[] filePaths = { Paths.get("src/test/resources/annotations-test/disable-test.bal") };

    @Test
    public void disableFunctionsTest() {
        BTestRunner testRunner = new BTestRunner();
        testRunner.runTest(filePaths, null);
        Assert.assertEquals(testRunner.getTesterinaReport().getPassedTestCount(), 3);
        Assert.assertEquals(testRunner.getTesterinaReport().getFailedTestCount(), 0);
        Assert.assertEquals(testRunner.getTesterinaReport().getSkippedTestCount(), 2);
    }
}
