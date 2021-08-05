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
 * Test parsing unary expression.
 */
public class UnaryExpressionTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testSimpleUnaryExpr() {
        test("+ 3", "unary-expr/unary_expr_assert_01.json");
        test("- 3", "unary-expr/unary_expr_assert_02.json");
        test("~ 3", "unary-expr/unary_expr_assert_03.json");
        test("! 3", "unary-expr/unary_expr_assert_04.json");
    }

    @Test
    public void testUnaryExprRecursively() {
        test("+ + 3", "unary-expr/unary_expr_assert_05.json");
        test("~ + - ! 3", "unary-expr/unary_expr_assert_06.json");
    }

    @Test
    public void testUnaryExpr() {
        test("+ 8 + 3", "unary-expr/unary_expr_assert_07.json");
    }

    @Test
    public void testComplexUnaryExprOpPrecedence() {
        test("~ a + b / !c + + d", "unary-expr/unary_expr_assert_08.json");
    }

    @Test
    public void testUnaryExprWithNegation() {
        test("!isNotFound", "unary-expr/unary_expr_assert_12.json");
        testFile("unary-expr/unary_expr_source_13.bal", "unary-expr/unary_expr_assert_13.json");
    }

    // Recovery test

    @Test
    public void testUnaryExprWithMissingExpr() {
        test("+", "unary-expr/unary_expr_assert_09.json");
        test("~ +", "unary-expr/unary_expr_assert_10.json");
        test("~ 3 + +", "unary-expr/unary_expr_assert_11.json");
    }
}
