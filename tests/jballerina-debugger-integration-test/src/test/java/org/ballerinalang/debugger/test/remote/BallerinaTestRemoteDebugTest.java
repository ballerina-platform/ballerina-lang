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
package org.ballerinalang.debugger.test.remote;

import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Test class to test positive scenarios of remote debugging ballerina test command.
 */
@Test(enabled = false)
public class BallerinaTestRemoteDebugTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        testProjectName = "basic-project";
        testModuleName = "hello-world";
        projectPath = testProjectBaseDir + File.separator + testProjectName;
    }

    @Test(enabled = false)
    public void testSuspendOnBallerinaModuleTest() throws BallerinaTestException {
        int port = findFreePort();
        String msg = "Listening for transport dt_socket at address: " + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.debugMain("test", new String[]{"--debug", String.valueOf(port), testModuleName}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath, 10);
        clientLeecher.waitForText(20000);
    }
}
