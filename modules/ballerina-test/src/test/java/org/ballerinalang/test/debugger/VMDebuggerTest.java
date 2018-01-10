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

import org.ballerinalang.test.utils.debug.Step;
import org.ballerinalang.test.utils.debug.VMDebuggerUtil;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.ballerinalang.test.utils.debug.Step.RESUME;
import static org.ballerinalang.test.utils.debug.Step.STEP_IN;
import static org.ballerinalang.test.utils.debug.Step.STEP_OUT;
import static org.ballerinalang.test.utils.debug.Step.STEP_OVER;

/**
 * Test Cases for {@link org.ballerinalang.util.debugger.VMDebugManager}.
 */
public class VMDebuggerTest {

    private static final String FILE = "test-debug.bal";
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
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE,
                3, 16, 27, 28, 31, 33, 35, 41, 42, 43, 44, 45);
        Step[] debugCommand = {RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 3, 16, 41, 28, 35, 42, 43);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 4, 7, 51, 37);
        Step[] debugCommand = {};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 5, 8, 41);
        Step[] debugCommand = {STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, RESUME, STEP_IN,
                STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 5, 12, 13, 14, 15, 19, 13, 8, 41, 25, 26, 28, 29, 35, 36, 42, 43, 9);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 25);
        Step[] debugCommand = {STEP_OUT, STEP_OUT, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 25, 42, 9);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 3);
        Step[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 3, 5, 6, 8, 9, 10);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 26);
        Step[] debugCommand = {STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 26, 28, 29, 35, 36, 42);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 13, 19, 21);
        Step[] debugCommand = {STEP_OVER, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME,
                RESUME, RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                FILE, 13, 14, 19, 13, 19, 13, 19, 13, 19, 13, 21);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints,
                                  expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing while statement resume")
    public void testWhileStatementResume() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "while-statement.bal", 5);
        Step[] debugCommand = {RESUME, RESUME, RESUME, RESUME, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "while-statement.bal", 5, 5, 5, 5, 5);
        VMDebuggerUtil.startDebug("test-src/debugger/while-statement.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }

    @Test(description = "Testing try catch finally scenario for path")
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22);
        Step[] debugCommand = {STEP_IN, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER,
                STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22, 30, 32,
                34, 35, 36, 37, 38, 46, 47, 48, 53, 58, 59, 61, 63);
        VMDebuggerUtil.startDebug("test-src/debugger/try-catch-finally.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }
}
