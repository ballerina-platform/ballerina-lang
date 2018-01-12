/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test Util class to test debug scenarios.
 *
 * @since 0.90
 */
public class VMDebuggerUtil {

    public static void startDebug(String sourceFilePath, BreakPointDTO[] breakPoints, BreakPointDTO[] expectedPoints,
                                  Step[] debugCommand) {
        TestDebugClientHandler debugClientHandler = new TestDebugClientHandler();
        TestDebugServer debugServer = new TestDebugServer();
        VMDebugManager debugManager = setupProgram(sourceFilePath, debugClientHandler, debugServer, breakPoints);

        if (!debugServer.tryAcquireLock(1000)) {
            Assert.fail("VM doesn't start within 1000ms");
        }

        debugManager.resume(debugClientHandler.getThreadId());

        for (int i = 0; i <= expectedPoints.length; i++) {
            debugClientHandler.aquireSem();
            if (i < expectedPoints.length) {
                NodeLocation expected = new NodeLocation(expectedPoints[i].getFileName(),
                        expectedPoints[i].getLineNumber());
                Assert.assertEquals(debugClientHandler.haltPosition, expected,
                        "Unexpected halt position for debug step " + (i + 1));
                executeDebuggerCmd(debugManager, debugClientHandler, debugCommand[i]);
            } else {
                Assert.assertTrue(debugClientHandler.isExit, "Debugger didn't exit as expected.");
            }
        }
    }

    public static BreakPointDTO[] createBreakNodeLocations(String packagePath, String fileName, int... lineNos) {
        BreakPointDTO[] breakPointDTOS = new BreakPointDTO[lineNos.length];
        int i = 0;
        for (int line : lineNos) {
            breakPointDTOS[i] = new BreakPointDTO(packagePath, fileName, line);
            i++;
        }
        return breakPointDTOS;
    }

    public static void executeDebuggerCmd(VMDebugManager debugManager,
                                          TestDebugClientHandler debugClientHandler, Step cmd) {
        switch (cmd) {
            case STEP_IN:
                debugManager.stepIn(debugClientHandler.getThreadId());
                break;
            case STEP_OVER:
                debugManager.stepOver(debugClientHandler.getThreadId());
                break;
            case STEP_OUT:
                debugManager.stepOut(debugClientHandler.getThreadId());
                break;
            case RESUME:
                debugManager.resume(debugClientHandler.getThreadId());
                break;
            default:
                throw new IllegalStateException("Unknown Command");
        }
    }

    private static VMDebugManager setupProgram(String sourceFilePath, TestDebugClientHandler clientHandler,
                                               TestDebugServer debugServer, BreakPointDTO[] breakPoints) {
        CompileResult result = BCompileUtil.compile(sourceFilePath);

        VMDebugManager debugManager = result.getProgFile().getDebugManager();
        debugManager.setDebugEnabled(true);

        String[] args = {"Hello", "World"};
        DebuggerExecutor executor = new DebuggerExecutor(result, args, clientHandler, debugServer,
                new ArrayList<>(Arrays.asList(breakPoints)));
        (new Thread(executor)).start();
        return debugManager;
    }
}
