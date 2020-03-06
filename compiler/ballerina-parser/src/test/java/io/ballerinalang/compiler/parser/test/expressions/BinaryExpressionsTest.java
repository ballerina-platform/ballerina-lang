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
 * Test parsing expressions.
 */
public class BinaryExpressionsTest extends AbstractExpressionsTest {

    @Test
    public void testSimpleBinaryExpression() {
        test("a + b", "binary-expr/simple_binary_expr_assert_1.json");
        test("a + b - c", "binary-expr/simple_binary_expr_assert_2.json");
    }

    @Test
    public void testSimpleBinaryOpPrecedence() {
        test("a * b - c", "binary-expr/binary_expr_op_precedence_assert_1.json");
    }

    @Test
    public void testMultipleBinaryOpPrecedence() {
        test("a + b < c - d /  e", "binary-expr/binary_expr_op_precedence_assert_2.json");
    }

    @Test
    public void testBinaryOpPrecedenceWithParenthesis() {
        test("(a + b) * (c - d) / e", "binary-expr/binary_expr_op_precedence_assert_3.json");
    }

    @Test
    public void testComplexBinaryOpPrecedence() {
        test("a + b * c * d *g *h - e / f", "binary-expr/binary_expr_op_precedence_assert_4.json");
    }
}
