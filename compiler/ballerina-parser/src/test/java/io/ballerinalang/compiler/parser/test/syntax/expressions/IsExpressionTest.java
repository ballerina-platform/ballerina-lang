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
 * Test parsing is expression.
 */
public class IsExpressionTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testSimpleIsExpr() {
        test("3 is int", "is-expr/is_expr_assert_01.json");
    }

    @Test
    public void testIsExpr() {
        test("3 is int is decimal", "is-expr/is_expr_assert_02.json");
    }

    @Test
    public void testIsExprOpPrecedence() {
        test("a == b / c is int + d", "is-expr/is_expr_assert_03.json");
    }

    // Recovery test

    @Test
    public void testIsExprWithMissingTypeDescriptor() {
        test("3 is", "is-expr/is_expr_assert_04.json");
    }

    @Test
    public void testIsExprWithMissingExpr() {
        test("is int", "is-expr/is_expr_assert_05.json");
    }

    @Test
    public void testIsExprRecovery() {
        test("x a is b c is d", "is-expr/is_expr_assert_06.json");
    }
}
