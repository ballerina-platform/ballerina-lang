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
package org.ballerinalang.test.debugger;

import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test Cases for {@link org.ballerinalang.util.debugger.VMDebugManager}.
 */
public class VMDebuggerTest {

    private static final String FILE = "testDebug.bal";
    private PrintStream original;

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
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 3, 41, 35);
        Step[] debugCommand = {Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 3, 41, 35, 41);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 4, 7, 51, 37);
        Step[] debugCommand = {};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 5, 8, 41);
        Step[] debugCommand = {Step.STEP_IN, Step.RESUME, Step.RESUME, Step.STEP_IN, Step.STEP_IN, Step.RESUME,
                Step.RESUME, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 5, 13, 5, 8, 41, 25, 41, 8);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 25);
        Step[] debugCommand = {Step.STEP_OUT, Step.STEP_OUT, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 25, 41, 8);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Over.", enabled = false)
    public void testStepOver() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 3);
        Step[] debugCommand = {Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER,
                Step.STEP_OVER, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 3, 5, 6, 8, 9, 10);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in IfCondition.", enabled = false)
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 26);
        Step[] debugCommand = {Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER,
                Step.STEP_OVER, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 26, 28, 29, 35, 36, 41);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 13, 19, 21);
        Step[] debugCommand = {Step.STEP_OVER, Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME,
                Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 13, 14, 19, 13, 19, 13, 19, 13, 19, 13, 21);
        VMDebuggerUtil.startDebug("test-src/debugger/testDebug.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing while statement resume", enabled = false)
    public void testWhileStatementResume() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "while-statement.bal", 5);
        Step[] debugCommand = {Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "while-statement.bal", 5, 5, 5, 5, 5);
        VMDebuggerUtil.startDebug("test-src/debugger/while-statement.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing try catch finally scenario for path", enabled = false)
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22);
        Step[] debugCommand = {Step.STEP_IN, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER,
                Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER,
                Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.STEP_OVER, Step.RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22, 30, 32,
                34, 35, 36, 37, 38, 46, 47, 48, 53, 58, 59, 61, 63);
        VMDebuggerUtil.startDebug("test-src/debugger/try-catch-finally.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }
}
