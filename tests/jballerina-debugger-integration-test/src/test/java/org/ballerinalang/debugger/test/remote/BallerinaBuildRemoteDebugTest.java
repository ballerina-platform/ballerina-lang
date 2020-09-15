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
 * Test class to test positive and negative scenarios of remote debugging ballerina build command.
 */
@Test(enabled = false)
public class BallerinaBuildRemoteDebugTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private String singleFilePath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        testProjectName = "basic-project";
        testModuleName = "hello-world";
        projectPath = testProjectBaseDir + File.separator + testProjectName;
        singleFilePath = testSingleFileBaseDir.toString();
    }

    @Test(enabled = false)
    public void testSuspendOnBallerinaModuleBuild() throws BallerinaTestException {
        int port = findFreePort();
        String msg = "Listening for transport dt_socket at address: " + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.debugMain("build", new String[]{"--debug", String.valueOf(port), testModuleName}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath, 20);
        clientLeecher.waitForText(20000);
    }

    @Test(enabled = false)
    public void testSuspendOnBallerinaProjectBuild() throws BallerinaTestException {
        int port = findFreePort();
        String msg = "Listening for transport dt_socket at address: " + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.debugMain("build", new String[]{"--debug", String.valueOf(port), "-a"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath, 20);
        clientLeecher.waitForText(20000);
    }

    /**
     * Negative test scenario to ensure that the remote debug suspend doesn't work when "--skip-tests" flag is used
     * in the build command.
     *
     * @throws BallerinaTestException if the tests runs in remote debug mode, even when the  "--skip-tests" flag is
     *                                present.
     */
    @Test(enabled = false)
    public void negativeTestSuspendOnBallerinaModuleBuildWithSkipFlag() throws BallerinaTestException {
        int port = findFreePort();
        String msg = "Listening for transport dt_socket at address: " + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        try {
            balClient.debugMain("build", new String[]{"--debug", String.valueOf(port), "--skip-tests",
                            testModuleName},
                    null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath, 20);
            clientLeecher.waitForText(20000);
            throw new BallerinaTestException("Ballerina tests running is suspended even when the test skip flag is " +
                    "used.");
        } catch (BallerinaTestException e) {
            if (!e.getMessage().contains("Program execution in debug mode is finished without receiving the" +
                    " suspend log.")) {
                throw e;
            }
        }
    }
}
