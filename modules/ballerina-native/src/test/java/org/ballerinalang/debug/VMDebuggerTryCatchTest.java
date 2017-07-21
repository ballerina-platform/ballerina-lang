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

import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test Cases for {@link org.ballerinalang.util.debugger.VMDebugManager}.
 *
 * @since 0.90
 */
public class VMDebuggerTryCatchTest {

    private static final String STEP_IN = "1";
    private static final String STEP_OVER = "2";
    private static final String STEP_OUT = "3";
    private static final String RESUME = "5";
    private static final String EXIT = "0";
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

    @Test(description = "Testing try catch finally scenario for path")
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22);
        String[] debugCommand = {STEP_IN, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER,
                STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME};
        BreakPointDTO[] expectedBreakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 22, 30, 32,
                34, 35, 36, 37, 38, 46, 47, 48, 53, 58, 59, 61, 63);
        VMDebuggerUtil.startDebug("lang/debug/try-catch-finally.bal", breakPoints,
                expectedBreakPoints, debugCommand);
    }
}
