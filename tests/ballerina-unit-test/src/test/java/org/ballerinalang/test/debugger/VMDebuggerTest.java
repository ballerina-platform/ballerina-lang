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

import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.test.utils.debug.DebugPoint;
import org.ballerinalang.test.utils.debug.ExpectedResults;
import org.ballerinalang.test.utils.debug.Util;
import org.ballerinalang.test.utils.debug.VMDebuggerUtil;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.test.utils.debug.Step.RESUME;
import static org.ballerinalang.test.utils.debug.Step.STEP_IN;
import static org.ballerinalang.test.utils.debug.Step.STEP_OUT;
import static org.ballerinalang.test.utils.debug.Step.STEP_OVER;
import static org.ballerinalang.test.utils.debug.Util.createBreakNodeLocations;

/**
 * Test Cases for {@link Debugger}.
 */
public class VMDebuggerTest {

    private static final String FILE = "test-debug.bal";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
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
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE,
                3, 9, 17, 29, 30, 33, 35, 37, 42, 43, 44, 45, 46, 47);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 3, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 17, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 42, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 43, RESUME, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "p", SUCCESS, "10");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 7, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 4, 7, 51, 39);

        List<DebugPoint> debugPoints = new ArrayList<>();

        ExpectedResults expRes = new ExpectedResults(debugPoints, 0, 0, new ArrayList<>(), false);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 5, 8, 41);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 5, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 12, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 13, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 15, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 16, STEP_IN, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "x", SUCCESS, "15");
        populateExpressionMap(expMap1, "y", SUCCESS, "5");
        populateExpressionMap(expMap1, "z", SUCCESS, "0");
        populateExpressionMap(expMap1, "a", SUCCESS, "6");
        populateExpressionMap(expMap1, "k", FAILURE, "cannot find variable 'k'");
        debugPoints.add(Util.createDebugPoint(".", FILE, 20, STEP_IN, 1, expMap1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 8, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 25, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 27, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 28, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 31, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 38, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 42, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 43, STEP_IN, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap2 = new HashMap<>();
        populateExpressionMap(expMap2, "p", SUCCESS, "10");
        populateExpressionMap(expMap2, "r", SUCCESS, "100");
        populateExpressionMap(expMap2, "s", SUCCESS, "large");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap2));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 21, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 26);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, STEP_OUT, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "p", SUCCESS, "10");
        populateExpressionMap(expMap1, "q", SUCCESS, "5");
        populateExpressionMap(expMap1, "r", SUCCESS, "100");
        populateExpressionMap(expMap1, "s", SUCCESS, "large");
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 3, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 3);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 3, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 5, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 6, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 8, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 9, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 10, RESUME, 1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 6, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 26);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 26, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 27, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 28, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 30, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 31, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 37, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 38, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 41, RESUME, 1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 8, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", FILE, 13, 14, 20, 22);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", FILE, 13, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", FILE, 14, RESUME, 5));
        debugPoints.add(Util.createDebugPoint(".", FILE, 20, RESUME, 4));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "x", SUCCESS, "15");
        populateExpressionMap(expMap1, "y", SUCCESS, "5");
        populateExpressionMap(expMap1, "z", SUCCESS, "100");
        populateExpressionMap(expMap1, "a", SUCCESS, "10");
        debugPoints.add(Util.createDebugPoint(".", FILE, 22, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 11, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing while statement resume")
    public void testWhileStatementResume() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".",
                "while-statement.bal", 5);

        List<DebugPoint> debugPoints = new ArrayList<>();
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "args", SUCCESS, "Array[2] [\\\"Hello\\\", \\\"World\\\"]");
        debugPoints.add(Util.createDebugPoint(".", "while-statement.bal", 5, RESUME, 5, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("i", "Local", new BInteger(4)));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 5, 0, variables, true);

        VMDebuggerUtil.startDebug("test-src/debugger/while-statement.bal", breakPoints, expRes);
    }

    @Test(description = "Testing try catch finally scenario for path")
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", "try-catch-finally.bal", 19);

        String file = "try-catch-finally.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 19, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 27, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 32, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 33, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 34, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 43, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 44, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 45, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 50, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 55, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 56, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 58, STEP_OVER, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "path", SUCCESS, "start insideTry insideInnerTry onError " +
                "innerTestErrorCatch:test innerFinally TestErrorCatch Finally ");
        debugPoints.add(Util.createDebugPoint(".", file, 60, RESUME, 1, expMap1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 16, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/try-catch-finally.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in workers")
    public void testDebuggingWorkers() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", "test-worker.bal", 3, 9, 10, 18, 19, 23, 48);

        String file = "test-worker.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 9, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 10, STEP_OVER, 1));

        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "a", FAILURE, "cannot find variable 'a'");
        populateExpressionMap(expMap1, "p", SUCCESS, "15");
        populateExpressionMap(expMap1, "q", SUCCESS, "5");
        debugPoints.add(Util.createDebugPoint(".", file, 12, STEP_IN, 1, expMap1));
        debugPoints.add(Util.createDebugPoint(".", file, 30, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 13, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 18, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 19, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 48, RESUME, 5));

        // Key: expression, Value: expected results
        Map<String, String> expMap2 = new HashMap<>();
        populateExpressionMap(expMap2, "a", SUCCESS, "0");
        populateExpressionMap(expMap2, "p", SUCCESS, "15");
        populateExpressionMap(expMap2, "q", SUCCESS, "5");
        populateExpressionMap(expMap2, "b", SUCCESS, "100");
        debugPoints.add(Util.createDebugPoint(".", file, 23, RESUME, 1, expMap2));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 15, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-worker.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in package init")
    public void testDebuggingPackageInit() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", "test-package-init.bal", 3, 9);

        String file = "test-package-init.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 5, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 13, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 14, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 16, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 17, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 21, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 16, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 17, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 21, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 15, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 23, RESUME, 1));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "val1", SUCCESS, "60");
        populateExpressionMap(expMap1, "val2", SUCCESS, "20");
        populateExpressionMap(expMap1, "cal", SUCCESS, "80");
        debugPoints.add(Util.createDebugPoint(".", file, 9, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("val1", "Global", new BInteger(60)));
        variables.add(Util.createVariable("val2", "Global", new BInteger(20)));
        variables.add(Util.createVariable("cal", "Local", new BInteger(80)));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 15, 2, variables, true);

        VMDebuggerUtil.startDebug("test-src/debugger/test-package-init.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug match statement and objects")
    public void testDebuggingMatchAndObject() {
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", "test_object_and_match.bal", 3, 48, 66, 54);

        String file = "test_object_and_match.bal";

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 3, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 7, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 23, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 26, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 30, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 32, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 8, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 37, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 51, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 9, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 29, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 10, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 39, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 40, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 11, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 12, STEP_IN, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 35, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 36, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 39, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 42, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 43, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 44, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 13, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 14, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 15, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 48, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 16, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 66, RESUME, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 54, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 67, STEP_OUT, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 4, RESUME, 1));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 37, 0, new ArrayList<>(), false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_object_and_match.bal", breakPoints, expRes);
    }

    @Test(description = "Testing global variables availability in debug hit message")
    public void testGlobalVarAvailability() {
        String file = "test_variables.bal";
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", file, 13);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 13, STEP_OVER, 1));
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "gInt", SUCCESS, "5");
        populateExpressionMap(expMap1, "gBool", SUCCESS, "true");
        populateExpressionMap(expMap1, "y", SUCCESS, "25");
        populateExpressionMap(expMap1, "foo", SUCCESS, "Record Foo {count:5, last:\\\"last\\\"}");
        debugPoints.add(Util.createDebugPoint(".", file, 14, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("gInt", "Global", new BInteger(5)));
        variables.add(Util.createVariable("gStr", "Global", new BString("str")));
        variables.add(Util.createVariable("gBool", "Global", new BBoolean(true)));
        variables.add(Util.createVariable("gByte", "Global", new BByte((byte) 255)));
        variables.add(Util.createVariable("gNewStr", "Global", new BString("ABCDEFG HIJ")));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 2, 7, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_variables.bal", breakPoints, expRes);
    }

    @Test(description = "Testing local variables scopes")
    public void testLocalVarScope() {
        String file = "test_variables.bal";
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", file, 12);

        List<DebugPoint> debugPoints = new ArrayList<>();
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "gStr", SUCCESS, "str");
        populateExpressionMap(expMap1, "gByte", SUCCESS, "255");
        populateExpressionMap(expMap1, "x", SUCCESS, "10");
        populateExpressionMap(expMap1, "z", SUCCESS, "15");
        populateExpressionMap(expMap1, "args", SUCCESS, "Array[2] [\\\"Hello\\\", \\\"World\\\"]");
        debugPoints.add(Util.createDebugPoint(".", file, 12, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("gInt", "Global", new BInteger(5)));
        variables.add(Util.createVariable("gStr", "Global", new BString("str")));
        variables.add(Util.createVariable("gBool", "Global", new BBoolean(true)));
        variables.add(Util.createVariable("gByte", "Global", new BByte((byte) 255)));
        variables.add(Util.createVariable("gNewStr", "Global", new BString("ABCDEFG HIJ")));
        variables.add(Util.createVariable("args", "Local", new BStringArray(new String[]{"Hello", "World"})));
        variables.add(Util.createVariable("x", "Local", new BInteger(10)));
        variables.add(Util.createVariable("z", "Local", new BInteger(15)));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 1, 7, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_variables.bal", breakPoints, expRes);
    }

    @Test(description = "Test debugging when multi-packages available")
    public void testMultiPackage() {
        String file = "apple.bal";
        String packagePath = "abc/fruits:0.0.1";
        BreakPointDTO[] breakPoints = createBreakNodeLocations(packagePath, file, 9);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(packagePath, file, 9, RESUME, 1));

        List<VariableDTO> variables = new ArrayList<>();
        String objName = "Apple";
        variables.add(Util.createVariable("self", "Local", createObject(objName, packagePath)));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 1, 0, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/multi-package/main.bal", breakPoints, expRes);
    }

    @Test(description = "Test evaluating global variables from other packages")
    public void testEvaluatingOtherPackageGlobalVars() {
        String file = "apple.bal";
        String packagePath = "abc/fruits:0.0.1";
        BreakPointDTO[] breakPoints = createBreakNodeLocations(packagePath, file, 9);

        List<DebugPoint> debugPoints = new ArrayList<>();
        // Key: expression, Value: expected results
        Map<String, String> expMap1 = new HashMap<>();
        populateExpressionMap(expMap1, "abc/fruits:0.0.1:gInt", SUCCESS, "10");
        populateExpressionMap(expMap1, "abc/fruits:0.0.1:gApple", SUCCESS, "Object Apple {}");
        populateExpressionMap(expMap1, "abc/vegetables:0.0.1:gInt", FAILURE,
                "cannot find variable 'abc/vegetables:0.0.1:gInt'");
        debugPoints.add(Util.createDebugPoint(packagePath, file, 9, RESUME, 1, expMap1));

        List<VariableDTO> variables = new ArrayList<>();
        String objName = "Apple";
        variables.add(Util.createVariable("self", "Local", createObject(objName, packagePath)));

        ExpectedResults expRes = new ExpectedResults(debugPoints, 1, 0, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/multi-package/main.bal", breakPoints, expRes);
    }

    @Test(description = "Test ignoring non-nullable global variables with null values")
    public void testGlobalVariableNullability() {
        String file = "test_variables.bal";
        BreakPointDTO[] breakPoints = createBreakNodeLocations(".", file, 31);

        List<DebugPoint> debugPoints = new ArrayList<>();
        debugPoints.add(Util.createDebugPoint(".", file, 31, STEP_OVER, 1));
        debugPoints.add(Util.createDebugPoint(".", file, 32, RESUME, 1));

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(Util.createVariable("gInt", "Global", new BInteger(5)));
        variables.add(Util.createVariable("gStr", "Global", new BString("str")));
        variables.add(Util.createVariable("gBool", "Global", new BBoolean(true)));
        variables.add(Util.createVariable("gNewStr", "Global", new BString("")));
        variables.add(Util.createVariable("gByte", "Global", new BByte((byte) 0)));

        // Expected global variables count should be 6 in this case
        // Reason: Variable 'gPerson' is not yet initialized. Thus, its current value is null
        // But this is a non-nullable variable and hence we omit this when adding variables to the frame
        ExpectedResults expRes = new ExpectedResults(debugPoints, 2, 6, variables, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test_variables.bal", breakPoints, expRes);
    }

    /**
     * Creates and returns a pseudo object with empty flags and fields.
     *
     * @param objectName  object name
     * @param packagePath package path eg. orgName/packageName:version
     * @return Object representation
     */
    private BMap createObject(String objectName, String packagePath) {
        BObjectType bObjectType;
        ObjectTypeInfo objectTypeInfo = new ObjectTypeInfo();
        bObjectType = new BObjectType(objectTypeInfo, objectName, packagePath, 0);
        bObjectType.setFields(new LinkedHashMap<>());
        objectTypeInfo.setType(bObjectType);
        return new BMap(bObjectType);
    }

    private void populateExpressionMap(Map<String, String> expMap, String expression, String status, String expected) {
        String jsonResults = "{\"status\":\"" + status + "\", \"results\":\"" + expected + "\"}";
        expMap.put(expression, jsonResults);
    }
}
