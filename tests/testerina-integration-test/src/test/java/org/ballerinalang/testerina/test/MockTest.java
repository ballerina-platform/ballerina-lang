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
public class MockTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private String projectPath2;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = mockProjectPath.toString();
        projectPath2 = mockProjectPath2.toString();
    }

    @Test()
    public void testFunctionMocking() throws BallerinaTestException {
        String msg1 = "9 passing";
        String msg2 = "3 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        balClient.runMain("test", new String[]{"Mock"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test()
    public void testObjectMocking() throws BallerinaTestException {
        String msg1 = "5 passing";
        String msg2 = "6 failing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        balClient.runMain("test", new String[]{"object-mocking"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath2);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }
}
