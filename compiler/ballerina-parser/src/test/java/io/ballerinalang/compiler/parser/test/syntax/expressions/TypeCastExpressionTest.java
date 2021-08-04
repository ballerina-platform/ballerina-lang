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
 * Test parsing type cast expression.
 */
public class TypeCastExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testSimpleTypeCast() {
        test("<int> b", "type-cast-expr/type_cast_expr_assert_01.json");
        test("<@foo{}> b", "type-cast-expr/type_cast_expr_assert_02.json");
        test("<@foo{} int> b", "type-cast-expr/type_cast_expr_assert_03.json");
    }

    @Test
    public void testTypeCast() {
        test("<int> <decimal> a+b", "type-cast-expr/type_cast_expr_assert_04.json");
        test("<int> a < b", "type-cast-expr/type_cast_expr_assert_05.json");
        test("<int> a < <decimal> b", "type-cast-expr/type_cast_expr_assert_06.json");
        test("<@foo:bar{a:b} @bar{} int> c + d", "type-cast-expr/type_cast_expr_assert_07.json");
    }

    @Test
    public void testTypeCastOpPrecedence() {
        test("<int> a + <int> b", "type-cast-expr/type_cast_expr_assert_19.json");
    }

    @Test
    public void testWithSingletonTypeIntFloatLiterals() {
        testFile("type-cast-expr/type_cast_expr_source_20.bal", "type-cast-expr/type_cast_expr_assert_20.json");
    }

    // Recovery tests

    @Test
    public void testTypeCastWithMissingExpr() {
        test("<int>;", "type-cast-expr/type_cast_expr_assert_08.json");
        test("<@foo{}>;", "type-cast-expr/type_cast_expr_assert_09.json");
        test("<@foo{} int>;", "type-cast-expr/type_cast_expr_assert_10.json");
    }

    @Test
    public void testTypeCastWithMissingParam() {
        test("< > b", "type-cast-expr/type_cast_expr_assert_11.json");
    }

    @Test
    public void testTypeCastWithMissingParamAndExpr() {
        test("< >", "type-cast-expr/type_cast_expr_assert_12.json");
    }

    @Test
    public void testTypeCastWithMissingGTToken() {
        test("<int b", "type-cast-expr/type_cast_expr_assert_13.json");
        // Here first b will be captured as a type-descriptor
        test("<@foo{} b b", "type-cast-expr/type_cast_expr_assert_14.json");
        test("<@foo{} b ", "type-cast-expr/type_cast_expr_assert_15.json");
    }

    @Test
    public void testTypeCastWithMissingLTToken() {
        test("int> b", "type-cast-expr/type_cast_expr_assert_16.json");
    }
}
