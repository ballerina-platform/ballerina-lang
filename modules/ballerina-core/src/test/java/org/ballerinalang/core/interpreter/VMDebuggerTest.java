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

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

/**
 * Test Cases for {@link BLangExecutionDebugger}.
 */
public class VMDebuggerTest {

    private static final String STEP_IN = "1";
    private static final String STEP_OVER = "2";
    private static final String STEP_OUT = "3";
    private static final String RESUME = "5";
    private static final String EXIT = "0";
    private static final String FILE = "testDebug.bal";
    private PrintStream original;

    private static void startDebug(NodeLocation[] breakPoints, NodeLocation[] expectedPoints, String[] debugCommand) {
        DebugRunner debugRunner = new DebugRunner();
        debugRunner.setup();
        DebugSessionObserverImpl debugSessionObserver = new DebugSessionObserverImpl();
//        debugRunner.debugger.setDebugSessionObserver(debugSessionObserver);
//        debugRunner.debugger.addDebugPoints(breakPoints);

        debugRunner.start();

        for (int i = 0; i <= expectedPoints.length; i++) {
            if (i < expectedPoints.length) {
                debugSessionObserver.aquireSem();
                Assert.assertEquals(debugSessionObserver.haltPosition, expectedPoints[i],
                        "Unexpected halt position for debug step " + (i + 1));
                executeDebuggerCmd(debugRunner, debugCommand[i]);
            } else {
                debugSessionObserver.aquireSem();
                Assert.assertTrue(debugSessionObserver.isExit, "Debugger didn't exit as expected.");
            }
        }
    }

    private static NodeLocation[] createBreakNodeLocations(String fileName, int... lineNos) {
        NodeLocation[] nodeLocations = new NodeLocation[lineNos.length];
        int i = 0;
        for (int line : lineNos) {
            nodeLocations[i] = new NodeLocation(fileName, line);
            i++;
        }
        return nodeLocations;
    }

    private static void executeDebuggerCmd(DebugRunner debugRunner, String cmd) {
        switch (cmd) {
            case STEP_IN:
//                debugRunner.debugger.stepIn();
                break;
            case STEP_OVER:
//                debugRunner.debugger.stepOver();
                break;
            case STEP_OUT:
//                debugRunner.debugger.stepOut();
                break;
            case RESUME:
//                debugRunner.debugger.resume();
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
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 3, 41, 35);
        String[] debugCommand = {RESUME, RESUME, RESUME};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 3, 41, 35);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 4, 7, 51, 37);
        String[] debugCommand = {};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 5, 8, 41);
        String[] debugCommand = {STEP_IN, RESUME, STEP_IN, RESUME, STEP_IN, RESUME};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 5, 12, 8, 39, 41, 24);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 24);
        String[] debugCommand = {STEP_OUT, STEP_OUT, RESUME};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 24, 42, 9);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 3);
        String[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 3, 5, 6, 8, 9);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 26);
        String[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 26, 29, 35, 36, 42);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        NodeLocation[] breakPoints = createBreakNodeLocations(FILE, 13, 19, 21);
        String[] debugCommand = {STEP_OVER, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME};
        NodeLocation[] expectedBreakPoints = createBreakNodeLocations(FILE, 13, 14, 19, 19, 19, 19, 21);
        startDebug(breakPoints, expectedBreakPoints, debugCommand);
    }

    static class DebugRunner extends Thread {

        ProgramFile programFile;
        Context bContext;
//        BLangVMDebugger debugger;

        void setup() {
            ModeResolver.getInstance().setNonblockingEnabled(true);
            String sourceFilePath = "samples/debug/testDebug.bal";
            Path path;
            
            try {
                path = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("error while running test: " + e.getMessage());
            }

            programFile = new BLangProgramLoader().loadMainProgramFile(path, Paths.get(sourceFilePath));


            bContext = new Context();
            bContext.setDebugInfoHolder(new DebugInfoHolder());

            ControlStackNew controlStackNew = bContext.getControlStackNew();
            String mainPkgName = programFile.getMainPackageName();

            PackageInfo mainPkgInfo = programFile.getPackageInfo(mainPkgName);
            if (mainPkgInfo == null) {
                throw new RuntimeException("cannot find main function '" + programFile.getProgramFilePath() + "'");
            }

            FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
            if (mainFuncInfo == null) {
                throw new RuntimeException("cannot find main function '" + programFile.getProgramFilePath() + "'");
            }

            // Invoke package init function
            BLangFunctions.invokeFunction(programFile, mainPkgInfo, mainPkgInfo.getInitFunctionInfo(), bContext);

            // Prepare main function arguments
            BStringArray arrayArgs = new BStringArray();
            arrayArgs.add(0, "Hello");
            arrayArgs.add(1, "World");

            WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();
            org.ballerinalang.bre.bvm.StackFrame stackFrame = new org.ballerinalang.bre.bvm.StackFrame(mainFuncInfo,
                    defaultWorkerInfo, -1, new int[0]);
            stackFrame.getRefLocalVars()[0] = arrayArgs;
            controlStackNew.pushFrame(stackFrame);

            BLangVM bLangVM = new BLangVM(programFile);
            bContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());

//            debugger = new BLangVMDebugger(programFile, bContext);
        }

        private void startDebug() {
//            debugger.run(bContext);
        }

        @Override
        public void run() {
            startDebug();
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
                e.printStackTrace();
            }
        }

        @Override
        public void notifyComplete() {
        }

        @Override
        public void notifyExit() {
            isExit = true;
        }

        @Override
        public void notifyHalt(BreakPointInfo breakPointInfo) {
            hitCount++;
            haltPosition = breakPointInfo.getHaltLocation();
            executionSem.release();
        }
    }
}
