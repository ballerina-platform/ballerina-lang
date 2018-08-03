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
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;

import static org.ballerinalang.BLangProgramRunner.COLON;
import static org.ballerinalang.BLangProgramRunner.MAIN;

/**
 * Test debug util class to test debug scenarios.
 *
 * @since 0.90
 */
public class VMDebuggerUtil {

    public static void startDebug(String programArgs, BreakPointDTO[] bPoints, ExpectedResults expRes) {

        TestDebugger debugger = setupProgram(programArgs, bPoints);

        if (!debugger.tryAcquireLock(1000)) {
            Assert.fail("VM doesn't start within 1000ms");
        }

        debugger.startDebug();

        int hitCount = 0;
        while (true) {
            debugger.getClientHandler().aquireSem();
            if (debugger.getClientHandler().isExit()) {
                break;
            }
            MessageDTO debugHit = debugger.getClientHandler().getDebugHit();
            DebugPoint debugPoint = expRes.getDebugHit(debugHit.getLocation());
            Assert.assertNotNull(debugPoint, "Invalid debug point hit - " + debugHit.getLocation());
            int hits = debugPoint.decrementAndGetHits();
            Assert.assertTrue(hits >= 0, "Invalid number of hits for same point - "
                    + debugPoint.getExpBreakPoint() + " remaining hit count - " + hits);
            hitCount++;
            executeDebuggerCmd(debugger, debugHit.getThreadId(), debugPoint.getNextStep());
        }
        Assert.assertEquals(hitCount, expRes.getDebugCount(), "Missing debug point hits - " + expRes);
    }

    private static void executeDebuggerCmd(Debugger debugManager, String workerId, Step cmd) {
        switch (cmd) {
            case STEP_IN:
                debugManager.stepIn(workerId);
                break;
            case STEP_OVER:
                debugManager.stepOver(workerId);
                break;
            case STEP_OUT:
                debugManager.stepOut(workerId);
                break;
            case RESUME:
                debugManager.resume(workerId);
                break;
            default:
                throw new IllegalStateException("Unknown Command");
        }
    }

    private static TestDebugger setupProgram(String programArg, BreakPointDTO[] breakPoints) {
        String srcPath = programArg;
        String functionName = MAIN;

        if (programArg.contains(COLON)) {
            //assumes one colon
            String[] programArgConstituents = programArg.split(COLON);
            srcPath = programArgConstituents[0];
            functionName = programArgConstituents[1];
        }

        CompileResult result = BCompileUtil.compile(srcPath);
        TestDebugger debugger = new TestDebugger(result.getProgFile());
        result.getProgFile().setDebugger(debugger);
        debugger.setDebugEnabled();

        String[] args = {"Hello", "World"};
        DebuggerExecutor executor = new DebuggerExecutor(result, functionName, args, debugger,
                                                         new ArrayList<>(Arrays.asList(breakPoints)));
        (new Thread(executor)).start();
        return debugger;
    }
}
