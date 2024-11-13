/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.adapter.run;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test class for debugger engaging in debug launch mode.
 *
 * @since 2.0.0
 */
public class DebugEngageTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;
    private static final String TEST_PROJECT_NAME = "basic-project";

    @Override
    @BeforeClass
    public void setup() {
    }

    @Test(description = "Debug engage test (launch mode), with special ballerina project files as inputs" +
            "(i.e. Ballerina.toml, Dependencies.toml, etc.)")
    public void testDebugLaunchWithNonBallerinaFiles() throws BallerinaTestException {
        String balTomlFile = "Ballerina.toml";
        debugTestRunner = new DebugTestRunner(TEST_PROJECT_NAME, balTomlFile, true);
        Path breakpointFilePath = debugTestRunner.testProjectPath.resolve("hello_world.bal");
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(breakpointFilePath, 19));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));
    }

    @Test(description = "Debug engage test (launch mode), with special Ballerina project files as inputs " +
            "(i.e. persist/model.bal, etc.)")
    public void testDebugLaunchWithBallerinaToolFile() throws BallerinaTestException {
        String balPersistModelFile = "persist/model.bal";
        debugTestRunner = new DebugTestRunner(TEST_PROJECT_NAME, balPersistModelFile, true);
        Path breakpointFilePath = debugTestRunner.testProjectPath.resolve("hello_world.bal");
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(breakpointFilePath, 19));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        // Test for debug engage
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
