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

import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestBreakPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.TestBreakPointListener;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Test class for ballerina breakpoints related test scenarios.
 */
public class ModuleBreakpointTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        testModuleName = "advanced";
        testModuleFileName = "main.bal";
        testSingleFileName = "hello_world.bal";
    }

    @Test
    public void testSingleModuleBreakPoint() throws BallerinaTestException {
        entryFilePath = Paths.get(testProjectPath.toString(), "src", testModuleName, testModuleFileName).toString();
        initDebugSession();
        List<BallerinaTestBreakPoint> breakPoints = new ArrayList<>();
        breakPoints.add(new BallerinaTestBreakPoint(entryFilePath, 18));
        setBreakpoints(breakPoints);
        launchDebuggee(DebugUtils.DebuggeeExecutionKind.RUN);
        List<BallerinaTestBreakPoint> capturedBreakpoints = waitForDebugHit(20000);
        Assert.assertEquals(capturedBreakpoints, breakPoints);
    }

    private List<BallerinaTestBreakPoint> waitForDebugHit(long timeoutMillis) {
        TestBreakPointListener bpListener = new TestBreakPointListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(bpListener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        return bpListener.getCapturedBreakpoints();
    }
}
