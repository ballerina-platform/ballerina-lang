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
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test Util class to test debug scenarios.
 *
 * @since 0.90
 */
public class VMDebuggerUtil {

    public static void startDebug(String srcPath, BreakPointDTO[] bPoints, ExpectedResults expRes) {

        TestDebugger debugger = setupProgram(srcPath, bPoints);

        if (!debugger.tryAcquireLock(1000)) {
            Assert.fail("VM doesn't start within 1000ms");
        }

        debugger.startDebug();

        Step currentStep = Step.RESUME;
        while (true) {
            debugger.getClientHandler().aquireSem();
            if (debugger.getClientHandler().isExit) {
                break;
            }
            checkDebugPointHit(expRes, debugger.getClientHandler().haltPosition, currentStep);
            currentStep = expRes.getCurrent().getNextStep();
            executeDebuggerCmd(debugger, debugger.getClientHandler(), currentStep);
        }
    }

    private static void checkDebugPointHit(ExpectedResults expRes, BreakPointDTO halt, Step crntStep) {
        BreakPointDTO expLocation;
        if (!expRes.isMultiThreaded() || !Step.RESUME.equals(crntStep)) {
            expLocation = expRes.getCurrent().getCurrentLocation();
            expRes.getCurrent().incrementPointer();
        } else {
            expLocation = findDebugPoint(expRes, halt);
        }
        Assert.assertEquals(halt, expLocation, "Unexpected halt location expected location - "
                + expLocation + ", actual location - " + halt);
    }

    private static BreakPointDTO findDebugPoint(ExpectedResults expRes, BreakPointDTO halt) {
        BreakPointDTO expLocation = findDebugPointInWorkerRes(expRes.getCurrent());
        if (halt.equals(expLocation)) {
            expRes.getCurrent().incrementPointer();
            return expLocation;
        }
        List<WorkerResults> workerResults = expRes.getWorkerResults();
        for (WorkerResults workerRes : workerResults) {
            if (workerRes == expRes.getCurrent()) {
                continue;
            }
            expLocation = findDebugPointInWorkerRes(workerRes);
            if (halt.equals(expLocation)) {
                workerRes.incrementPointer();
                expRes.setCurrent(workerRes);
                return expLocation;
            }
        }
        expRes.setCurrent(null);
        return null;
    }

    private static BreakPointDTO findDebugPointInWorkerRes(WorkerResults workerRes) {
        if (workerRes == null) {
            return null;
        }
        return workerRes.getCurrentLocation();
    }

    public static BreakPointDTO[] createWorkerBreakPoints(String packagePath, String fileName, int... lineNos) {
        BreakPointDTO[] breakPointDTOS = new BreakPointDTO[lineNos.length];
        int i = 0;
        for (int line : lineNos) {
            breakPointDTOS[i] = new BreakPointDTO(packagePath, fileName, line);
            i++;
        }
        return breakPointDTOS;
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

    private static void executeDebuggerCmd(Debugger debugManager,
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
                debugManager.resume();
                break;
            default:
                throw new IllegalStateException("Unknown Command");
        }
    }

    private static TestDebugger setupProgram(String sourceFilePath, BreakPointDTO[] breakPoints) {
        CompileResult result = BCompileUtil.compile(sourceFilePath);

        TestDebugger debugger = new TestDebugger(result.getProgFile());
        result.getProgFile().setDebugger(debugger);
        debugger.setDebugEnabled();

        String[] args = {"Hello", "World"};
        DebuggerExecutor executor = new DebuggerExecutor(result, args, debugger,
                new ArrayList<>(Arrays.asList(breakPoints)));
        (new Thread(executor)).start();
        return debugger;
    }
}
