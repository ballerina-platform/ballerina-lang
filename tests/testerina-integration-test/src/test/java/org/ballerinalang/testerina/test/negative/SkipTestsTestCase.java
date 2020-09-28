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
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for missing before,after and dependsOn functions.
 */
public class SkipTestsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFilesProjectPath.resolve("skip-tests").toString();
    }

    @Test
    public void testSkipWhenDependsOnFunctionFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "1 failing";
        String msg3 = "2 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"dependson-skip-test.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenBeforeFails() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-before-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenAfterFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "1 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-after-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenBeforeEachFails() throws BallerinaTestException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-beforeEach-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }


    @Test
    public void testSkipWhenAfterEachFails() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String msg3 = "2 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-afterEach-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenBeforeSuiteFails() throws BallerinaTestException {
        String msg1 = "0 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-beforeSuite-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenBeforeGroupsFails() throws BallerinaTestException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String msg3 = "3 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-beforeGroups-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

    @Test
    public void testSkipWhenAfterGroupsFails() throws BallerinaTestException {
        String msg1 = "4 passing";
        String msg2 = "0 failing";
        String msg3 = "1 skipped";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        balClient.runMain("test", new String[]{"skip-when-afterGroups-fails.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }
}
