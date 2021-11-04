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
    private static final String HINT_UNNECESSARY_CONDITION_FOR_NEVER =
            "unnecessary condition: expression will always evaluate to 'true' for variable of type 'never'";
    private static final String ALWAYS_FALSE_CONDITION = "expression will always evaluate to 'false'";
    private static final String ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED =
            "expression of type 'never' or equivalent to type 'never' not allowed here";

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
                "testReachableCodeWithForeach",
                "testReachableCodeWithUnaryConditionsInIf",
                "testReachableCodeWithTypeNarrowing"
        };
    }

    @Test
    public void testUnreachability() {
        CompileResult result = BCompileUtil.compile("test-src/reachability-analysis/unreachability_test.bal");
        int i = 0;
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 19, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 27, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 36, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 44, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 56, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 61, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 75, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 87, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 96, 9);
        validateWarning(result, i++, "unused variable 'a'", 111, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 117, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 120, 9);
        validateWarning(result, i++, "unused variable 'a'", 127, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 133, 15);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 135, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 136, 9);
        validateWarning(result, i++, "unused variable 'a'", 143, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 149, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 153, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 166, 1);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 176, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 181, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 184, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 191, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 194, 13);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 208, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 212, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 216, 1);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 220, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 225, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 233, 13);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 239, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 244, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 250, 1);
        validateWarning(result, i++, "unused variable 'str'", 255, 5);
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
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 326, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 334, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 341, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 350, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 357, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 369, 13);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 377, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 378, 9);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 387, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 388, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 399, 9);
        validateWarning(result, i++, "unused variable 'a'", 399, 9);
        validateWarning(result, i++, "unused variable 'a'", 401, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 413, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 416, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 421, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 431, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 439, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 442, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 445, 5);
        validateWarning(result, i++, "unused variable 'res'", 450, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 459, 17);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 461, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 464, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 468, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 482, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 486, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 499, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 504, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 513, 5);
        validateWarning(result, i++, "unused variable 'x'", 513, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 518, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 533, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 540, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 553, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 560, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 568, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 576, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 586, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 595, 9);
        validateWarning(result, i++, "unused variable 'x'", 600, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 604, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 611, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 621, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 630, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 637, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 642, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 649, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 658, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 663, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 670, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 679, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 682, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 690, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 704, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 720, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 730, 9);
        validateError(result, i++, ERROR_MUST_RETURN_A_RESULT, 733, 1);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 741, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 755, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 771, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 779, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 782, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 782, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 791, 15);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 793, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 794, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 794, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 803, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 806, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 806, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 815, 15);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 817, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 818, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 818, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 829, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 829, 19);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 839, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 840, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 840, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 850, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 853, 5);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 853, 13);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 858, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 863, 5);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 863, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 873, 5);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 873, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 883, 5);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 883, 16);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 888, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 891, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 891, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 901, 15);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 903, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 904, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 904, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 914, 15);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 916, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 917, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 917, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 918, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 919, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 919, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 929, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 932, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 932, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 941, 27);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 943, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 944, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 944, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 952, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 953, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 953, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION_FOR_NEVER, 962, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 963, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 963, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 973, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 973, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 982, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 982, 19);
        Assert.assertEquals(result.getErrorCount(), i - 35 - 9);
        Assert.assertEquals(result.getHintCount(), 35);
        Assert.assertEquals(result.getWarnCount(), 9);
    }

    @Test
    public void testSemanticsInNarrowingWithSingleIfNotCompletedNoramlly() {
        CompileResult result = BCompileUtil.compile(
                "test-src/reachability-analysis/narrowing_with_if_without_else_not_completed_normally_test.bal");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'int', found 'string'", 25, 17);
        validateError(result, i++, "incompatible types: expected 'boolean', found '(int|string|boolean)'", 28, 17);
        validateError(result, i++, "incompatible types: expected '(int|string)', found 'boolean'", 52, 20);
        validateError(result, i++, "incompatible types: expected 'boolean', found '(int|string|boolean)'", 65, 21);
        validateError(result, i++, "incompatible types: expected 'int', found 'boolean'", 67, 13);
        validateError(result, i++, "incompatible types: expected 'int', found 'string'", 81, 21);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string)'", 89, 16);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string)'", 110, 16);
        validateError(result, i++, "incompatible types: expected 'int', found 'string'", 123, 17);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string|boolean)'", 125, 16);
        validateError(result, i++, "incompatible types: expected 'boolean', found 'float'", 137, 21);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string)'", 140, 16);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string)'", 156, 16);
        validateError(result, i++, "incompatible types: expected 'boolean', found 'boolean?'", 170, 25);
        validateError(result, i++, "incompatible types: expected 'int', found 'int?'", 173, 17);
        validateError(result, i++, "incompatible types: expected 'string', found '(int|string)'", 176, 16);
        validateError(result, i++, "incompatible types: expected 'int', found 'string'", 190, 13);
        validateError(result, i++, "incompatible types: expected 'int', found '(int|string)'", 196, 13);
        validateError(result, i++, "incompatible types: expected 'string', found 'int'", 198, 16);
        validateError(result, i++, "incompatible types: expected 'string', found 'string?'", 201, 16);
        validateError(result, i++, "incompatible types: expected '10', found '20'", 211, 12);
        validateError(result, i++, "incompatible types: expected '20', found '10'", 221, 12);
        validateError(result, i++, "incompatible types: expected '10', found '20'", 234, 12);
        validateError(result, i++, "incompatible types: expected '20', found '10'", 244, 12);
        validateError(result, i++, "incompatible types: expected '()', found 'int'", 252, 12);
        validateError(result, i++, "incompatible types: expected 'int', found 'int?'", 254, 13);
        validateError(result, i++, "incompatible types: expected '()', found 'int'", 262, 12);
        validateError(result, i++, "incompatible types: expected 'int', found 'int?'", 264, 13);
        validateError(result, i++, "incompatible types: expected '20', found '10'", 272, 12);
        validateError(result, i++, "incompatible types: expected '10', found '10|20'", 274, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
