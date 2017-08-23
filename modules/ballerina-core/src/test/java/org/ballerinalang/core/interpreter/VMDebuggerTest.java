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
package org.ballerinalang.core.interpreter;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.DebuggerExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Test Cases for {@link org.ballerinalang.util.debugger.VMDebugManager}.
 */
public class VMDebuggerTest {

    private static final String STEP_IN = "1";
    private static final String STEP_OVER = "2";
    private static final String STEP_OUT = "3";
    private static final String RESUME = "5";
    private static final String EXIT = "0";
    private static final String FILE = "testDebug.bal";
    private PrintStream original;

    private static void startDebug(BreakPointDTO[] breakPoints, BreakPointDTO[] expectedPoints, String[] debugCommand) {
        DebugSessionObserverImpl debugSessionObserver = new DebugSessionObserverImpl();
        DebugRunner debugRunner = new DebugRunner();
        Context bContext = debugRunner.setup(debugSessionObserver, breakPoints);

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

    private static BreakPointDTO[] createBreakNodeLocations(String fileName, int... lineNos) {
        BreakPointDTO[] breakPointDTOS = new BreakPointDTO[lineNos.length];
        int i = 0;
        for (int line : lineNos) {
            breakPointDTOS[i] = new BreakPointDTO(".", fileName, line);
            i++;
        }
        return breakPointDTOS;
    }

    private static void executeDebuggerCmd(Context bContext, String cmd) {
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

    @BeforeClass
    public void setup() {
        original = System.out;
        // Hiding all test System outs.
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterClass
    public void tearDown() {
        System.setOut(original);
    }

    @Test(description = "Testing Resume with break points.")
    public void testResume() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 3, 41, 35);
        String[] debugCommand = {RESUME, RESUME, RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 3, 41, 35, 41);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 4, 7, 51, 37);
        String[] debugCommand = {};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 5, 8, 41);
        String[] debugCommand = {STEP_IN, RESUME, RESUME, STEP_IN, STEP_IN, RESUME, RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 5, 13, 5, 8, 41, 25, 41, 8);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 25);
        String[] debugCommand = {STEP_OUT, STEP_OUT, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 25, 41, 8);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 3);
        String[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 3, 5, 6, 8, 9, 10);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 26);
        String[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 26, 28, 29, 35, 36, 41);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(FILE, 13, 19, 21);
        String[] debugCommand = {STEP_OVER, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME,
                RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = createBreakNodeLocations(FILE, 13, 14, 19, 13, 19, 13, 19,
                13, 19, 13, 21);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    static class DebugRunner {

        ProgramFile programFile;
        Context bContext;

        Context setup(DebugSessionObserverImpl debugSessionObserver, BreakPointDTO[] breakPoints) {
            ModeResolver.getInstance().setNonblockingEnabled(true);
            String sourceFilePath = "samples/debug/testDebug.bal";

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
