/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.debugger.test.adapter.runinterminal;

import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class to test the runInTerminal feature for single files.
 */
public class SingleFileRunInTerminalTest extends BaseTestCase {
    DebugTestRunner debugTestRunner;
    boolean didRunInIntegratedTerminal;

    @BeforeMethod(alwaysRun = true)
    public void setup() throws BallerinaTestException {
        String testFolderName = "basic-project";
        String testSingleFileName = "hello_world.bal";
        debugTestRunner = new DebugTestRunner(testFolderName, testSingleFileName, false);
    }

    @Test(description = "Debug launch test in integrated terminal for single file")
    public void testRunInIntegratedTerminal() throws BallerinaTestException {
        String integratedTerminal = "integrated";
        didRunInIntegratedTerminal = debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN,
                integratedTerminal);
        Assert.assertTrue(didRunInIntegratedTerminal);
    }

    @Test(description = "Debug launch test in external terminal for single file")
    public void testRunInExternalTerminal() throws BallerinaTestException {
        String externalTerminal = "external";
        didRunInIntegratedTerminal = debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN,
                externalTerminal);

        // returned value should be true since external terminal requests are also handled as integrated terminal
        // requests, as of now.
        Assert.assertTrue(didRunInIntegratedTerminal);
    }

    @Test(description = "Debug launch test with invalid terminal kind")
    public void testRunInInvalidTerminal() throws BallerinaTestException {
        String invalidTerminalKind = "internal";
        didRunInIntegratedTerminal = debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN,
                invalidTerminalKind);

        // returned value should be false since internal is not a terminal kind that is accommodated for the
        // runinterminal request
        Assert.assertFalse(didRunInIntegratedTerminal);
    }

    @Test(description = "Debug launch test without runinterminal config")
    public void testLaunchWithoutConfig() throws BallerinaTestException {
        didRunInIntegratedTerminal = debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN,
                "");

        // returned value should be false since the runinterminal kind config wasn't set to launch the program in a
        // separate terminal
        Assert.assertFalse(didRunInIntegratedTerminal);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
