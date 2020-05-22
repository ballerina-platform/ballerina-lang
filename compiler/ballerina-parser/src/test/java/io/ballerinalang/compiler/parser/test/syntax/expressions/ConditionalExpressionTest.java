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
 * Test parsing conditional expression.
 */
public class ConditionalExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testSimpleElvisConditionalExpr() {
        test("a ?: b", "conditional-expr/conditional_expr_assert_01.json");
    }

    @Test
    public void testSimpleConditionalExpr() {
        test("a ? (b) : c", "conditional-expr/conditional_expr_assert_02.json");
    }

    @Test
    public void testConditionalExpr() {
        test("a+b ?: c+d ? (e) : f+g ?: h + k", "conditional-expr/conditional_expr_assert_03.json");
        test("a ? (b) : c ? (d) : e", "conditional-expr/conditional_expr_assert_04.json");
        test("a ? b:c : d", "conditional-expr/conditional_expr_assert_05.json");
        test("a ? b:c ?: d:e : f", "conditional-expr/conditional_expr_assert_06.json");
    }

    // Recovery tests

    @Test
    public void testElvisConditionalWithMissingExpr() {
        test("a ?:", "conditional-expr/conditional_expr_assert_07.json");
        test("?: b", "conditional-expr/conditional_expr_assert_08.json");
    }

    @Test
    public void testConditionalWithMissingEndExpr() {
        test("a ? b :", "conditional-expr/conditional_expr_assert_09.json");
    }

    @Test
    public void testConditionalWithMissingColon() {
        test("a ? (b)  c", "conditional-expr/conditional_expr_assert_10.json");
    }

    @Test
    public void testConditionalWithMissingMiddleExpr() {
        test("a ?  : c", "conditional-expr/conditional_expr_assert_11.json");
    }

    @Test
    public void testConditionalWithMissingQuestionMark() {
        test("a  b:d : d", "conditional-expr/conditional_expr_assert_12.json");
        test("a  (b) : c", "conditional-expr/conditional_expr_assert_13.json");
    }

    @Test
    public void testConditionalWithMissingInitialExpr() {
        test("?(b):c", "conditional-expr/conditional_expr_assert_14.json");
        test("{foo : ?(b):c}", "conditional-expr/conditional_expr_assert_15.json");
        test("[foo, ?(b):c]", "conditional-expr/conditional_expr_assert_16.json");
    }
}
