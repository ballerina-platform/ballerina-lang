/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.reachability;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateHint;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Test cases related to reachability analysis.
 *
 * @since 2.0.0
 */
public class ReachabilityAnalysisTest {
    private static final String ERROR_UNREACHABLE_CODE = "unreachable code";
    private static final String ERROR_MUST_RETURN_A_RESULT = "this function must return a result";
    private static final String HINT_UNNECESSARY_CONDITION =
            "unnecessary condition: expression will always evaluate to 'true'";
    private static final String ALWAYS_FALSE_CONDITION = "expression will always evaluate to 'false'";
    private static final String WARN_SHOULD_EXPLICITLY_RETURN_NIL =
            "this function should explicitly return a 'nil' value";

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/reachability-analysis/reachability_analysis.bal");
    }

    @Test(dataProvider = "reachabilityTests")
    public void testReachability(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "reachabilityTests")
    public Object[] reachabilityTests() {
        return new Object[]{
                "testReachableCodeWithIf2",
                "testReachableCodeWithIf3",
                "testReachableCodeWithIfElse1",
                "testReachableCodeWithIfElse2",
                "testReachableCodeWithIfElse3",
                "testReachableCodeWithIfElse4",
                "testReachableCodeWithNestedIfElse1",
                "testReachableCodeWithIfElseAndConditionalExpr",
                "testReachableCodeWithIfElseAndConditionalExpr2",
                "testReachableCodeWithWhile2",
                "testReachableCodeWithWhile3",
                "testReachableCodeWithWhile4",
                "testReachableCodeWithFail",
                "testWhileCompletingNormally",
                "testCallStmtFuncReturningNever",
                "testForeachCompletingNormally",
                "testReachableCodeWithForeach"
        };
    }

    @Test
    public void testUnreachability() {
        CompileResult result = BCompileUtil.compile("test-src/reachability-analysis/unreachability_test.bal");
        int i = 0;
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 18, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 26, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 35, 14);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 43, 14);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 60, 14);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 74, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 87, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 96, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 117, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 119, 12);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 133, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 135, 22);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 149, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 152, 12);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 166, 1);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 176, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 180, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 183, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 190, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 193, 16);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 208, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 212, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 216, 1);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 220, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 224, 22);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 233, 13);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 239, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 243, 17);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 250, 1);
        validateWarning(result, i++, WARN_SHOULD_EXPLICITLY_RETURN_NIL, 252, 48);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 263, 27);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 266, 17);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 268, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 275, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 285, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 289, 5);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 290, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 294, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 307, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 311, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 325, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 334, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 340, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 349, 28);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 356, 23);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 368, 24);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 377, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 377, 18);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 387, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 387, 21);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 398, 14);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 413, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 416, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 420, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 431, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 439, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 441, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 445, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 459, 17);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 461, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 464, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 468, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 482, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 486, 9);
        Assert.assertEquals(result.getErrorCount(), i - 16);
        Assert.assertEquals(result.getHintCount(), 15);
        Assert.assertEquals(result.getWarnCount(), 1);
    }
}
