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
 * Test parsing Error constructor expression.
 *
 * @since 1.3.0
 */
public class ErrorConstructorExprTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testWithoutTypeReference() {
        test("error(\"ERROR_REASON\", message = \"error message\")",
                "error-constructor-expr/error_constructor_expr_assert_08.json");
        test("error(\"ERROR_REASON\", \"error message\");",
                "error-constructor-expr/error_constructor_expr_assert_09.json");
        test("error(\"error message\");", "error-constructor-expr/error_constructor_expr_assert_05.json");
        test("error(\"ERROR_REASON\", \"error message\", code = errorCode, count = 10);",
                "error-constructor-expr/error_constructor_expr_assert_06.json");
    }

    @Test
    public void testWithTypeReference() {
        test("error errorType(\"ERROR_REASON\", message = \"error message\")",
                "error-constructor-expr/error_constructor_expr_assert_07.json");
        test("error Foo:errorType(\"ERROR_REASON\", message = \"error message\")",
                "error-constructor-expr/error_constructor_expr_assert_03.json");
    }

    @Test
    public void testWithAssignment() {
        testFile("error-constructor-expr/error_constructor_expr_source_01.bal",
                "error-constructor-expr/error_constructor_expr_assert_01.json");
    }

    // Recovery test

    @Test
    public void testWrongAssignment() {
        testFile("error-constructor-expr/error_constructor_expr_source_02.bal",
                "error-constructor-expr/error_constructor_expr_assert_02.json");
    }

    @Test
    public void testMissingCloseParen() {
        test("error(\"Test\";", "error-constructor-expr/error_constructor_expr_with_missing_elements_01.json");
        test("error(\"ERROR_REASON\", message = \"error message\";",
                "error-constructor-expr/error_constructor_expr_with_missing_elements_02.json");
    }

    @Test
    public void testMissingOpenParen() {
        test("error\"ERROR_REASON\", message = \"error message\");",
                "error-constructor-expr/error_constructor_expr_with_missing_elements_03.json");
    }

    @Test
    public void testMissingParenthesis() {
        test("error\"ERROR_REASON\", message = \"error message\";",
                "error-constructor-expr/error_constructor_expr_with_missing_elements_04.json");
    }

    @Test
    public void testRecoveryForErrorKeywordOnly() {
        test("error", "error-constructor-expr/error_constructor_expr_with_missing_elements_05.json");
    }

    @Test
    public void testExtraElements() {
        test("error type(\"message\")",
                "error-constructor-expr/error_constructor_expr_with_extra_elements_01.json");
        test("error(, message = \"error message\",)",
                "error-constructor-expr/error_constructor_expr_with_extra_elements_02.json");
    }

    @Test
    public void testWithOutArgs() {
        test("error()", "error-constructor-expr/error_constructor_expr_without_args.json");
    }

    @Test
    public void testWithNoPositionalArgs() {
        test("error(message = \"error message\")",
                "error-constructor-expr/error_constructor_expr_assert_04.json");
    }

    @Test
    public void testWithMoreThanTwoPositionalArgs() {
        test("error(\"msg1\", \"msg2\", \"msg3\", count = 3)",
                "error-constructor-expr/error_constructor_expr_assert_10.json");
    }

    @Test
    public void testWithRestArgs() {
        test("error(\"ERROR_REASON\", message = \"error message\", ... messages)",
                "error-constructor-expr/error_constructor_expr_assert_11.json");
    }

    @Test
    public void testWithInvalidNamedArg() {
        test("error(\"er:ror!\", err:code = 1001)",
                "error-constructor-expr/error_constructor_expr_assert_12.json");
        test("error(\"er:ror!\", e = 2,  err:code = 1001)",
                "error-constructor-expr/error_constructor_expr_assert_13.json");
    }
}
