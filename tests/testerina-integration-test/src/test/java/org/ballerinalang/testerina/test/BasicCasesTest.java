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
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class BasicCasesTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = basicTestsProjectPath.toString();
    }

    @Test
    public void testAssertTrue() throws BallerinaTestException {
        String msg = "2 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--groups", "p1", "assertions"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAllExceptAssertTrue() throws BallerinaTestException {
        String msg1 = "15 passing";
        String msg2 = "39 passing";
        String msg3 = "2 passing";
        String msg4 = "3 passing";
        String msg5 = "8 passing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);
        LogLeecher clientLeecher4 = new LogLeecher(msg4);
        LogLeecher clientLeecher5 = new LogLeecher(msg5);
        balClient.runMain("test", new String[]{"--disable-groups", "p1", "--all", "--", "--user.name=waruna"}, null,
                          new String[]{}, new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3,
                        clientLeecher4, clientLeecher5}, projectPath);
        clientLeecher1.waitForText(400000);
        clientLeecher2.waitForText(400000);
        clientLeecher3.waitForText(400000);
        clientLeecher4.waitForText(400000);
        clientLeecher5.waitForText(400000);
    }
}
