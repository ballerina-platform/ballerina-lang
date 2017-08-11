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
package org.ballerinalang.debug;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.DebuggerExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Test Util class to test debug scenarios.
 *
 * @since 0.90
 */
public class VMDebuggerUtil {

    private static final String STEP_IN = "1";
    private static final String STEP_OVER = "2";
    private static final String STEP_OUT = "3";
    private static final String RESUME = "5";

    public static void startDebug(String sourceFilePath, BreakPointDTO[] breakPoints, BreakPointDTO[] expectedPoints,
                                  String[] debugCommand) {
        DebugSessionObserverImpl debugSessionObserver = new DebugSessionObserverImpl();
        DebugRunner debugRunner = new DebugRunner();
        Context bContext = debugRunner.setup(sourceFilePath, debugSessionObserver, breakPoints);

        if (!waitTillDebugStarts(1000, bContext)) {
            Assert.fail("VM doesn't start within 1000ms");
        }

        bContext.getDebugInfoHolder().releaseLock();

        for (int i = 0; i <= expectedPoints.length; i++) {
            debugSessionObserver.aquireSem();
            if (i < expectedPoints.length) {
                NodeLocation expected = new NodeLocation(expectedPoints[i].getFileName(),
                        expectedPoints[i].getLineNumber());
                Assert.assertEquals(debugSessionObserver.haltPosition, expected,
                        "Unexpected halt position for debug step " + (i + 1));
                executeDebuggerCmd(bContext, debugCommand[i]);
            } else {
                Assert.assertTrue(debugSessionObserver.isExit, "Debugger didn't exit as expected.");
            }
        }
    }

    private static boolean waitTillDebugStarts(long maxhWait, Context bContext) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (bContext.getDebugInfoHolder().hasQueuedThreads()) {
                return true;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > maxhWait) {
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

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

    public static void executeDebuggerCmd(Context bContext, String cmd) {
        switch (cmd) {
            case STEP_IN:
                bContext.getDebugInfoHolder().stepIn();
                break;
            case STEP_OVER:
                bContext.getDebugInfoHolder().stepOver();
                break;
            case STEP_OUT:
                bContext.getDebugInfoHolder().stepOut();
                break;
            case RESUME:
                bContext.getDebugInfoHolder().resume();
                break;
            default:
                throw new IllegalStateException("Unknown Command");
        }
    }

    static class DebugRunner {

        ProgramFile programFile;
        Context bContext;

        Context setup(String sourceFilePath, DebugSessionObserverImpl debugSessionObserver,
                      BreakPointDTO[] breakPoints) {
            ModeResolver.getInstance().setNonblockingEnabled(true);

            programFile = BTestUtils.getProgramFile(sourceFilePath);

            bContext = new Context(programFile);
            bContext.setAndInitDebugInfoHolder(new DebugInfoHolder());

            ControlStackNew controlStackNew = bContext.getControlStackNew();
            String mainPkgName = programFile.getEntryPkgName();

            PackageInfo mainPkgInfo = programFile.getPackageInfo(mainPkgName);
            if (mainPkgInfo == null) {
                throw new RuntimeException("cannot find main function '" + programFile.getProgramFilePath() + "'");
            }

            FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
            if (mainFuncInfo == null) {
                throw new RuntimeException("cannot find main function '" + programFile.getProgramFilePath() + "'");
            }

            // Invoke package init function
            BLangFunctions.invokeFunction(programFile, mainPkgInfo.getInitFunctionInfo(), bContext);

            // Prepare main function arguments
            BStringArray arrayArgs = new BStringArray();
            arrayArgs.add(0, "Hello");
            arrayArgs.add(1, "World");

            WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();
            org.ballerinalang.bre.bvm.StackFrame stackFrame = new org.ballerinalang.bre.bvm.StackFrame(mainFuncInfo,
                    defaultWorkerInfo, -1, new int[0]);
            stackFrame.getRefLocalVars()[0] = arrayArgs;
            controlStackNew.pushFrame(stackFrame);
            bContext.setDebugEnabled(true);
            bContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());
            bContext.getDebugInfoHolder().setDebugSessionObserver(debugSessionObserver);
            bContext.getDebugInfoHolder().addDebugPoints(new ArrayList<>(Arrays.asList(breakPoints)));
            bContext.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.RESUME);
            DebuggerExecutor executor = new DebuggerExecutor(programFile, bContext);
            (new Thread(executor)).start();
            return bContext;
        }
    }

    static class DebugSessionObserverImpl implements DebugSessionObserver {

        boolean isExit;
        int hitCount = -1;
        NodeLocation haltPosition;
        private volatile Semaphore executionSem;

        DebugSessionObserverImpl() {
            executionSem = new Semaphore(0);
        }

        public void aquireSem() {
            try {
                executionSem.acquire();
            } catch (InterruptedException e) {

            }
        }

        @Override
        public void addContext(Context bContext) {

        }

        @Override
        public void notifyComplete() {
        }

        @Override
        public void notifyExit() {
            isExit = true;
            executionSem.release();
        }

        @Override
        public void notifyHalt(BreakPointInfo breakPointInfo) {
            hitCount++;
            haltPosition = breakPointInfo.getHaltLocation();
            executionSem.release();
        }
    }
}
