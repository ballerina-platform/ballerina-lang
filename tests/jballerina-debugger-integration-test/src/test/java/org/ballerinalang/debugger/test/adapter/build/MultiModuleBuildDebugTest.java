/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.adapter.build;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Test class for multi module related debug scenarios for build command.
 */
@Test(enabled = false)
public class MultiModuleBuildDebugTest extends DebugAdapterBaseTestCase {

    private String projectPath;

    @BeforeClass
    public void setup() {
        testProjectName = "breakpoint-tests";
        testModuleName = "foo";
        projectPath = testProjectBaseDir + File.separator + testProjectName;

        testModuleFileName = "tests" + File.separator + "main_test.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();
    }

    @Test(enabled = false)
    public void testMultiModuleBuildDebugScenarios() throws BallerinaTestException {
        int port = findFreePort();
        runDebuggeeProgram(projectPath, port);
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 24));
        initDebugSession(null, port);

        // Test for debug engage
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(20000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        terminateDebugSession();
    }
}
