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
package org.ballerinalang.debugger.test.adapter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.DeubgHitListener;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Timer;

/**
 * Test class for ballerina breakpoints related test scenarios.
 */
public class ModuleBreakpointTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        testProjectName = "breakpoint-tests";
        testModuleName = "foo";
        testModuleFileName = "mainBal" + File.separator + "main.bal";
        testSingleFileName = "hello_world.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();
    }

    @Test
    public void testMultipleBreakpointsInSameFile() throws BallerinaTestException {

        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 25));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 26));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo2 = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo2.getLeft(), testBreakpoints.get(1));

        terminateDebugSession();
    }

    private Pair<BallerinaTestDebugPoint, StoppedEventArguments> waitForDebugHit(long timeoutMillis)
            throws BallerinaTestException {
        DeubgHitListener listener = new DeubgHitListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(listener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        if (!listener.isDebugHitFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the debug hit");
        }
        return new ImmutablePair<>(listener.getDebugHitpoint(), listener.getDebugHitContext());
    }
}
