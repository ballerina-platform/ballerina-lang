/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing check expression.
 * 
 * @since 1.3.0
 */
public class CheckExpressionTest extends AbstractExpressionsTest {

    @Test
    public void testSimpleCheckExpr() {
        testFile("check-expr/check_expr_source_01.bal", "check-expr/check_expr_assert_01.json");
    }

    @Test
    public void testCheckExprPrecedence() {
        testFile("check-expr/check_expr_source_02.bal", "check-expr/check_expr_assert_02.json");
    }

    @Test
    public void testCheckExprWithOnConflictClause() {
        testFile("check-expr/check_expr_source_05.bal", "check-expr/check_expr_assert_05.json");
    }

    @Test
    public void testCheckExprWithJoinClause() {
        testFile("check-expr/check_expr_source_07.bal", "check-expr/check_expr_assert_07.json");
    }

    @Test
    public void testOnFailCheck() {
        testFile("check-expr/on_fail_check_source_01.bal",  "check-expr/on_fail_check_assert_01.json");
    }

    @Test
    public void testOnFailCheckpanic() {
        testFile("check-expr/on_fail_check_source_04.bal", "check-expr/on_fail_check_assert_04.json");
    }

    @Test
    public void testOnFailCheckWithReturn() {
        testFile("check-expr/on_fail_check_source_05.bal", "check-expr/on_fail_check_assert_05.json");
    }

    @Test
    public void testOnFailCheckpanicWithReturn() {
        testFile("check-expr/on_fail_check_source_06.bal", "check-expr/on_fail_check_assert_06.json");
    }

    @Test
    public void testOnFailCheckNextToAnOnConflictClause() {
        testFile("check-expr/on_fail_check_source_08.bal", "check-expr/on_fail_check_assert_08.json");
    }

    @Test
    public void testOnFailCheckNextToASelectClause() {
        testFile("check-expr/on_fail_check_source_10.bal", "check-expr/on_fail_check_assert_10.json");
    }

    @Test
    public void testOnFailCheckWithFunctionReturningCustomError() {
        testFile("check-expr/on_fail_check_source_14.bal", "check-expr/on_fail_check_assert_14.json");
    }

    @Test
    public void testOnFailCheckWithTernaryConditionalExpr() {
        testFile("check-expr/on_fail_check_source_15.bal", "check-expr/on_fail_check_assert_15.json");
    }

    // Recovery test

    @Test
    public void testCheckWithMissingExpr() {
        testFile("check-expr/check_expr_source_03.bal", "check-expr/check_expr_assert_03.json");
    }

    @Test
    public void testCheckWithMissingBinaryOp() {
        testFile("check-expr/check_expr_source_04.bal", "check-expr/check_expr_assert_04.json");
    }

    @Test
    public void testCheckExprWithIncompleteRemoteMethods() {
        testFile("check-expr/check_expr_source_06.bal", "check-expr/check_expr_assert_06.json");
    }

    @Test
    public void testOnFailCheckWithoutDoubleArrow() {
        testFile("check-expr/on_fail_check_source_02.bal", "check-expr/on_fail_check_assert_02.json");
    }

    @Test
    public void testOnFailCheckpanicWithoutDoubleArrow() {
        testFile("check-expr/on_fail_check_source_03.bal", "check-expr/on_fail_check_assert_03.json");
    }

    @Test
    public void testOnFailCheckWithoutOnKeyword() {
        testFile("check-expr/on_fail_check_source_11.bal", "check-expr/on_fail_check_assert_11.json");
    }

    @Test
    public void testOnFailCheckWithoutOnKeywordAndDoubleArrow() {
        testFile("check-expr/on_fail_check_source_12.bal", "check-expr/on_fail_check_assert_12.json");
    }

    @Test
    public void testOnFailCheckWithOnKeywordOnly() {
        testFile("check-expr/on_fail_check_source_13.bal", "check-expr/on_fail_check_assert_13.json");
    }

    @Test
    public void testOnFailWithExtraCharactersTest1() {
        testFile("check-expr/on_fail_check_source_07.bal", "check-expr/on_fail_check_assert_07.json");
    }

    @Test
    public void testOnFailWithExtraCharactersTest2() {
        testFile("check-expr/on_fail_check_source_09.bal", "check-expr/on_fail_check_assert_09.json");
    }

    @Test
    public void testOnFailWithMissingQuestionMarkInTernaryOp() {
        testFile("check-expr/on_fail_check_source_16.bal", "check-expr/on_fail_check_assert_16.json");
    }
}
