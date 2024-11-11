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
import org.testng.annotations.AfterClass;
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
    private CompileResult result2;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/reachability-analysis/reachability_analysis.bal");
        result2 = BCompileUtil.compile("test-src/reachability-analysis/reachability_analysis2.bal");
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
                "testReachableCodeWithTypeNarrowing",
                "testTerminatingAndNonTerminatingLoops",
                "testTypeNarrowingWithDifferentWhileCompletionStatus",
                "testTypeNarrowingWithEqualityInCondition",
                "testReachableStatementInQueryAction",
                "testUnreachablePanicStmt1",
                "testUnreachablePanicStmt2",
                "testUnreachablePanicStmt3",
                "testUnreachablePanicStmt4",
                "testUnreachablePanicStmt5",
                "testUnreachablePanicStmt6",
                "testUnreachablePanicStmt7",
                "testUnreachablePanicStmt8",
                "testUnreachablePanicStmt9",
                "testUnreachablePanicStmt10",
                "testReachabilityWithQueryAction",
                "testReachabilityWithIfElseHavingUnreachablePanic"
        };
    }

    @Test(dataProvider = "reachabilityTests2")
    public void testReachability2(String function) {
        BRunUtil.invoke(result2, function);
    }

    @DataProvider(name = "reachabilityTests2")
    public Object[] reachabilityTests2() {
        return new Object[]{
                "testReachabilityWithIfContainingReturn",
                "testReachabilityWithIfContainingReturn2",
                "testReachabilityWithIfContainingReturn3",
                "testReachabilityWithIfContainingReturn4",
                "testReachabilityWithIfContainingReturn5",
                "testReachabilityWithIfContainingReturn6",
                "testReachabilityWithIfContainingReturn7",
                "testReachabilityWithIfUsingConstInExpr1",
                "testReachabilityWithIfUsingConstInExpr2",
                "testReachabilityWithIfUsingConstInExpr3",
                "testReachabilityWithIfUsingConstInExpr4",
                "testReachabilityWithIfUsingConstInExpr5",
                "testReachabilityWithNestedIfContainingReturn",
                "testReachabilityWithIsExprNotConstantTrue",
                "testReachabilityWithIsExprNotConstantTrue2",
                "testReachabilityWithIsExprNotConstantTrue3",
                "testReachabilityWithIsExprNotConstantTrue4",
                "testReachabilityWithIsExprNotConstantTrue5",
                "testReachabilityWithIsExprNotConstantTrue6",
                "testReachabilityWithFailExpr",
                "testReachabilityWithFailExpr2",
                "testReachabilityWithFailExpr3",
                "testReachabilityWithFailExpr4",
                "testReachabilityWithFailExpr5",
                "testReachabilityWithFailExpr6",
                "testReachabilityWithFailExpr7",
                "testReachabilityWithResetTypeNarrowing",
                "testReachabilityWithResetTypeNarrowing2",
                "testReachabilityWithResetTypeNarrowing3",
                "testReachabilityWithResetTypeNarrowing4",
                "testReachabilityWithResetTypeNarrowing5",
                "testReachabilityWithResetTypeNarrowing6",
                "testReachabilityWithResetTypeNarrowing7",
                "testReachabilityWithResetTypeNarrowing8",
                "testReachabilityWithResetTypeNarrowing9",
                "testReachabilityWithResetTypeNarrowing10",
                "testReachabilityWithResetTypeNarrowing11",
                "testReachabilityWithResetTypeNarrowing12",
                "testReachabilityWithResetTypeNarrowing13",
                "testReachabilityWithResetTypeNarrowing14",
                "testReachabilityWithResetTypeNarrowing15",
                "testReachabilityWithResetTypeNarrowing16",
                "testReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr",
                "testReachabilityWithIfStmtWithAConditionUsingEnum",
                "testReachabilityWithIfStmtWithAConditionUsingEnum2",
                "testReachabilityWithIfStmtWithAConditionUsingEnum3",
                "testReachabilityWithIfStmtWithAConditionUsingEnum4",
                "testReachabilityWithIfStmtWithAConditionUsingEnum5",
                "testReachabilityWithWhile",
                "testReachabilityWithWhile2",
                "testReachabilityWithWhile3",
                "testReachabilityWithWhile4",
                "testReachabilityWithWhile5",
                "testReachabilityWithWhile6",
                "testReachabilityWithNestedWhile",
                "testReachabilityWhereTheIsExprInsideWhile",
                "testReachabilityWhereTheIsExprInsideWhile2",
                "testReachabilityWithWhileStmtWithEqualityExpr",
                "testReachabilityWithWhileStmtWithEqualityExpr2",
                "testReachabilityWithWhileStmtWithEqualityExpr3",
                "testReachabilityWithWhileStmtWithEqualityExpr4",
                "testReachabilityWithWhileStmtWithAConditionUsingEnum",
                "testReachabilityWithWhileStmtWithAConditionUsingEnum2",
                "testReachabilityAfterCheck",
                "testReachabilityAfterCheckPanic"
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
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 285, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 289, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 294, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 307, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 311, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 326, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 334, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 341, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 350, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 357, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 369, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 372, 5);
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
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 993, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1003, 5);
        validateWarning(result, i++, "this function should explicitly return a value", 1006, 56);
        validateWarning(result, i++, "this function should explicitly return a value", 1016, 56);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1031, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1041, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1051, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1059, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1067, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1077, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1087, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1097, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1105, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1113, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1121, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1129, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1137, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1147, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1156, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1166, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1177, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1188, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1200, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1211, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1224, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1235, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1242, 9);
        validateWarning(result, i++, "unused variable 'a'", 1242, 14);
        validateWarning(result, i++, "unused variable 'b'", 1242, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1254, 9);
        validateWarning(result, i++, "unused variable 'val1'", 1254, 17);
        validateWarning(result, i++, "unused variable 'val2'", 1254, 26);
        validateWarning(result, i++, "unused variable 'x'", 1259, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1262, 13);
        validateWarning(result, i++, "unused variable 'y'", 1262, 13);
        validateWarning(result, i++, "unused variable 'x'", 1272, 5);
        validateWarning(result, i++, "unused variable 'y'", 1275, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1278, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1288, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1296, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1305, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1314, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1321, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1329, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1339, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1347, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1355, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1362, 8);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1364, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1365, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1373, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1375, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1382, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1384, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1390, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1401, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1404, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1410, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1412, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1419, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1422, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1432, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1442, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1445, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1456, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1465, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1477, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1489, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1498, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1504, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1510, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1516, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1524, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1530, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1538, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1544, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1550, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1553, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1563, 15);
        validateError(result, i++, "variable 'y' may not have been initialized", 1569, 12);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1581, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1596, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1611, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1626, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1641, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1656, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1672, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1682, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1700, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1704, 13);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1720, 23);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1726, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1744, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1754, 13);
        Assert.assertEquals(result.getErrorCount(), i - 46 - 19);
        Assert.assertEquals(result.getHintCount(), 46);
        Assert.assertEquals(result.getWarnCount(), 19);
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
        validateError(result, i++, "incompatible types: expected '10', found 'Type1'", 274, 12);
        validateError(result, i++, "incompatible types: expected 'int', found '()'", 286, 13);
        validateError(result, i++, "incompatible types: expected 'int', found '()'", 296, 13);
        validateError(result, i++, "incompatible types: expected '()', found 'int?'", 308, 12);
        validateError(result, i++, "incompatible types: expected '()', found 'int?'", 322, 12);
        validateError(result, i++, "incompatible types: expected 'string?', found 'int?'", 333, 9);
        validateError(result, i++, "incompatible types: expected 'string?', found 'int?'", 347, 9);
        validateError(result, i++, "incompatible types: expected 'string?', found 'int?'", 363, 9);
        validateError(result, i++, "incompatible types: expected 'int', found 'boolean'", 373, 17);
        validateError(result, i++, "incompatible types: expected 'boolean', found 'float'", 377, 17);
        validateError(result, i++, "incompatible types: expected 'boolean', found 'float'", 391, 17);
        validateError(result, i++, "incompatible types: expected 'boolean', found 'float'", 407, 17);
        validateError(result, i++, "incompatible types: expected 'string', found 'int'", 416, 16);
        validateError(result, i++, "incompatible types: expected 'string', found 'int'", 425, 20);
        validateError(result, i++, "incompatible types: expected 'int', found 'string'", 429, 13);
        validateError(result, i++, "incompatible types: expected 'string', found 'string?'", 441, 30);

        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testUnreachability2() {
        CompileResult result = BCompileUtil.compile("test-src/reachability-analysis/unreachability_test2.bal");
        int i = 0;
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 19, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 24, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 33, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 47, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 55, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 67, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 75, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 79, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 89, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 97, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 106, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 110, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 121, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 133, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 159, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 176, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 181, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 184, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 193, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 196, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 202, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 206, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 214, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 217, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 223, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 226, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 237, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 240, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 251, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 255, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 264, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 267, 9);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 267, 19);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 281, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 285, 5);
        validateError(result, i++, ERROR_TYPE_NEVER_EXPRESSION_NOT_ALLOWED, 285, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 296, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 307, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 316, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 323, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 331, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 339, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 341, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 350, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 357, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 363, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 373, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 381, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 383, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 393, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 401, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 408, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 419, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 428, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 432, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 438, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 446, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 456, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 462, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 466, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 475, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 482, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 488, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 498, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 506, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 508, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 518, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 526, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 533, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 544, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 554, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 558, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 567, 23);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 570, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 574, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 584, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 589, 25);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 590, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 609, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 612, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 620, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 637, 8);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 641, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 646, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 654, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 661, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 669, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 677, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 684, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 692, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 700, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 707, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 718, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 724, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 732, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 741, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 748, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 758, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 768, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 776, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 784, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 792, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 796, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 805, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 811, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 815, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 827, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 829, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 838, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 853, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 859, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 863, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 869, 11);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 873, 5);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 881, 15);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 885, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 901, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 909, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 915, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 926, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 933, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 944, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 947, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 956, 26);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 960, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 963, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 973, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 978, 28);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 979, 9);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 990, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 991, 17);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 999, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1002, 13);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1009, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1013, 9);
        validateHint(result, i++, HINT_UNNECESSARY_CONDITION, 1019, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1023, 9);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1032, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1040, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1049, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1057, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1067, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1087, 13);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1106, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1127, 13);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 1141, 16);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1142, 17);
        validateHint(result, i++, ALWAYS_FALSE_CONDITION, 1152, 19);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1153, 17);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1205, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1218, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1227, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1240, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1256, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1272, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1280, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1295, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1306, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1314, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1327, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1335, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1355, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1363, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1376, 5);
        validateError(result, i++, ERROR_UNREACHABLE_CODE, 1387, 5);

        Assert.assertEquals(result.getErrorCount(), i - 24);
        Assert.assertEquals(result.getHintCount(), 24);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        result2 = null;
    }
}
