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
package io.ballerinalang.compiler.parser.test.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing access expressions.
 */
public class AccessExpressionsTest extends AbstractExpressionsTest {

    @Test
    public void testFieldAccessExpression() {
        test("x + a.b.c.d + y", "access-expr/field_access_expr_assert_01.json");
    }

    @Test
    public void testMethodCallExpression() {
        test("x + a.b().c.d() + y", "access-expr/field_access_expr_assert_02.json");
    }

    @Test
    public void testNestedMethodCallExpression() {
        test("x + a.b(p.q(s.t()) + r).c.d() + y", "access-expr/field_access_expr_assert_03.json");
    }

    // Recovery tests

    @Test
    public void testMethodCallRecovery() {
        test("x + a b() c.d() + y", "access-expr/field_access_expr_assert_04.json");
    }

    @Test
    public void testAccessExpressionRecovery() {
        test("x + a b() c. d", "access-expr/field_access_expr_assert_05.json");
    }

}
