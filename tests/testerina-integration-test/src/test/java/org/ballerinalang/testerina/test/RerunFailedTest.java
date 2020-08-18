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
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class containting tests related to Rerun failed test functionality.
 */
public class RerunFailedTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = rerunFailedProjectPath.toString();
    }

    @Test
    public void testFullTest() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "2 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);

        balClient.runMain("test", new String[]{"module_1"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);

        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test (dependsOnMethods = "testFullTest")
    public void testRerunFailedTest() throws BallerinaTestException {
        String msg1 = "0 passing";
        String msg2 = "2 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);

        balClient.runMain("test", new String[]{"--rerun-failed", "module_1"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);

        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test
    public void testRerunWithNoFailedTests() throws BallerinaTestException {
        String msg1 = "No failed test/s found in cache";
        LogLeecher clientLeecher = new LogLeecher(msg1);

        balClient.runMain("test", new String[]{"--rerun-failed", "module_2"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);

        clientLeecher.waitForText(20000);
    }
}
