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
 * Test parsing binary expressions.
 */
public class BinaryExpressionsTest extends AbstractExpressionsTest {

    @Test
    public void testSimpleBinaryExpression() {
        test("a + b", "binary-expr/simple_binary_expr_assert_1.json");
        test("a + b - c", "binary-expr/simple_binary_expr_assert_2.json");
    }

    @Test
    public void testSimpleShiftExpression() {
        test("a << b", "binary-expr/shift_expr_assert_1.json");
        test("a >> b ", "binary-expr/shift_expr_assert_2.json");
        test("a >>> b ", "binary-expr/shift_expr_assert_3.json");
    }

    @Test
    public void testShiftExpression() {
        test("a <<b> c", "binary-expr/shift_expr_assert_4.json");
        test("a <<<b> c", "binary-expr/shift_expr_assert_5.json");
        test("a <<<map<int>> b >> c", "binary-expr/shift_expr_assert_6.json");
        test("<map<map<int>>> b >>> c", "binary-expr/shift_expr_assert_7.json");
    }

    @Test
    public void testSimpleRangeExpression() {
        test("a ... b", "binary-expr/range_expr_assert_1.json");
        test("a ..< b", "binary-expr/range_expr_assert_2.json");
        test("1...10", "binary-expr/range_expr_assert_5.json");
        test("1..<10", "binary-expr/range_expr_assert_6.json");
    }

    @Test
    public void testRangeExpression() {
        test("a ..<<int> b", "binary-expr/range_expr_assert_3.json");
        test("a ..<<map<int>> b", "binary-expr/range_expr_assert_4.json");
    }

    @Test
    public void testSimpleNumericalComparisonExpression() {
        test("a < b", "binary-expr/numerical_comparison_expr_assert_1.json");
        test("a > b ", "binary-expr/numerical_comparison_expr_assert_2.json");
        test("a <= b ", "binary-expr/numerical_comparison_expr_assert_3.json");
        test("a >= b ", "binary-expr/numerical_comparison_expr_assert_4.json");
    }

    @Test
    public void testSimpleEqualityExpression() {
        test("a == b", "binary-expr/equality_expr_assert_1.json");
        test("a != b ", "binary-expr/equality_expr_assert_2.json");
        test("a === b ", "binary-expr/equality_expr_assert_3.json");
        test("a !== b ", "binary-expr/equality_expr_assert_4.json");
    }

    @Test
    public void testSimpleBitwiseExpression() {
        test("a & b", "binary-expr/binary_bitwise_expr_assert_1.json");
        test("a ^ b ", "binary-expr/binary_bitwise_expr_assert_2.json");
        test("a | b ", "binary-expr/binary_bitwise_expr_assert_3.json");
    }

    @Test
    public void testSimpleLogicalExpression() {
        test("a && b", "binary-expr/logical_expr_assert_1.json");
        test("a || b ", "binary-expr/logical_expr_assert_2.json");
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

    @Test
    public void testComplexBinaryOpPrecedenceUpToEquality() {
        test("a - b != c === d >= e + f * g < h", "binary-expr/binary_expr_op_precedence_assert_5.json");
    }

    @Test
    public void testComplexBinaryOpPrecedenceUpToLogicalOr() {
        test("a || b && c | d ^ e & f !== g >= h ... m >> n + i / j",
                "binary-expr/binary_expr_op_precedence_assert_6.json");
    }
}
