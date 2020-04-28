/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.listener;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.BFileUtil;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases to verify launch listener init and shutdown failures.
 *
 * @since 1.3.0
 */
public class LaunchListenerTestCase extends BaseTest {

    @Test(description = "Case1: Test launch listener for init failure with runtime exception")
    public void testInitRuntimeFailure() throws BallerinaTestException {

        verify("test-launch-listener-01.jar", "hello_world_service.bal", "Oh no, something really went wrong.");
    }

    @Test(description = "Case2: Test launch listener for init failure with error value")
    public void testInitError() throws BallerinaTestException {

        verify("test-launch-listener-02.jar", "hello_world_service.bal", "error: An error in beforeRunProgram method");
    }

    @Test(description = "Case3: Test launch listener for shutdown failure with runtime exception")
    public void testShutdownRuntimeFailure() throws BallerinaTestException {

        verify("test-launch-listener-03.jar", "hello_world.bal", "Oh no, something really went wrong.");
    }

    @Test(description = "Case4: Test launch listener for shutdown failure with error value")
    public void testShutdownError() throws BallerinaTestException {

        verify("test-launch-listener-04.jar", "hello_world.bal", "error: An error in afterRunProgram method");
    }

    private void verify(String jarName, String balFileName, String serverResponse) throws BallerinaTestException {

        Path launchListenerJarFilePath = Paths.get("build", "launch-listener", "libs", jarName);

        assert launchListenerJarFilePath.toFile().exists();

        BalServer balServer = new BalServer();
        BMainInstance balClient = new BMainInstance(balServer);

        Path serverBreLib = Paths.get(balServer.getServerHome(), "bre", "lib", jarName);
        BFileUtil.copy(launchListenerJarFilePath, serverBreLib);

        Path balFilepath = Paths.get("src", "test", "resources", "listener");
        String balFile = Paths.get(balFilepath.toString(), balFileName).toFile().getAbsolutePath();

        LogLeecher clientLeecher = new LogLeecher(serverResponse, LogLeecher.LeecherType.ERROR);

        balClient.runMain("run", new String[]{balFile}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, balFilepath.toString());
        clientLeecher.waitForText(20000);
    }
}
