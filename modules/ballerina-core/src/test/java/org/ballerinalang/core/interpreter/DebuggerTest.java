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
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.builder.BLangExecutionFlowBuilder;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.nodes.StartNode;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Cases for {@link BLangExecutionDebugger}
 */
public class DebuggerTest {

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
        debugRunner.debugger.setDebugSessionObserver(debugSessionObserver);
        debugRunner.debugger.addDebugPoints(breakPoints);

        debugRunner.startDebug();
        for (int i = 0; i <= expectedPoints.length; i++) {
            if (i < expectedPoints.length) {
                Assert.assertEquals(debugSessionObserver.haltPosition, expectedPoints[i],
                        "Unexpected halt position for debug step " + (i + 1));
                executeDebuggerCmd(debugRunner, debugCommand[i]);
            } else {
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
                debugRunner.debugger.stepIn();
                break;
            case STEP_OVER:
                debugRunner.debugger.stepOver();
                break;
            case STEP_OUT:
                debugRunner.debugger.stepOut();
                break;
            case RESUME:
                debugRunner.debugger.resume();
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

    static class DebugRunner {

        BLangProgram bLangProgram;
        FunctionInvocationExpr funcIExpr;
        BLangExecutionDebugger debugger;

        void setup() {
            ModeResolver.getInstance().setNonblockingEnabled(true);
            String sourceFilePath = "samples/debug/testDebug.bal";
            Path path = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            bLangProgram = new BLangProgramLoader().loadMain(path, Paths.get(sourceFilePath));
            // Arguments for main function.
            BArray<BString> arrayArgs = new BArray<>(BString.class);
            arrayArgs.add(0, new BString("Hello"));
            arrayArgs.add(1, new BString("World"));


            Context bContext = new Context();
            BallerinaFunction mainFunction = bLangProgram.getMainFunction();

            mainFunction.accept(new BLangExecutionFlowBuilder());

            BValue[] argValues = new BValue[mainFunction.getStackFrameSize()];
            BValue[] cacheValues = new BValue[mainFunction.getTempStackFrameSize()];

            argValues[0] = arrayArgs;

            CallableUnitInfo functionInfo = new CallableUnitInfo(mainFunction.getName(), mainFunction.getPackagePath(),
                    mainFunction.getNodeLocation());

            funcIExpr = new FunctionInvocationExpr(
                    mainFunction.getNodeLocation(), mainFunction.getName(), null, null, new Expression[0]);
            funcIExpr.setOffset(argValues.length);
            funcIExpr.setCallableUnit(mainFunction);
            // Flow Building.
            BLangExecutionFlowBuilder flowBuilder = new BLangExecutionFlowBuilder();
            funcIExpr.setParent(new StartNode(StartNode.Originator.TEST));
            funcIExpr.accept(flowBuilder);

            StackFrame stackFrame = new StackFrame(argValues, new BValue[0], cacheValues, functionInfo);
            bContext.getControlStack().pushFrame(stackFrame);

            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
            debugger = new BLangExecutionDebugger(runtimeEnv, bContext);
        }

        public void startDebug() {
            debugger.execute(funcIExpr);
        }
    }

    static class DebugSessionObserverImpl implements DebugSessionObserver {

        boolean isExit;
        int hitCount = -1;
        NodeLocation haltPosition;

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
        }
    }
}
