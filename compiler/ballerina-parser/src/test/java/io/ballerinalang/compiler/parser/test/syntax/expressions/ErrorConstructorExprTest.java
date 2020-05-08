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
    public void testWithOutArgs() {
        test("error()", "functional-constructor-expr/error-constructor-expr/error_constructor_expr_without_args.json");
    }

    @Test
    public void testWithArgs() {
        test("error(\"ERROR_REASON\", message = \"error message\")", "functional-constructor-expr/error-constructor" +
                "-expr/error_constructor_expr_with_args_01.json");
        test("error(\"ERROR_REASON\", \"error message\");", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_args_02.json");
    }

    @Test
    public void testWithAssignment() {
        testFile("functional-constructor-expr/error-constructor-expr/error_constructor_expr_source_01.bal",
                "functional-constructor-expr/error-constructor-expr/error_constructor_expr_assert_01.json");
    }

    //Invalid syntax
    @Test
    public void testWrongAssignment() {
        testFile("functional-constructor-expr/error-constructor-expr/error_constructor_expr_source_02.bal",
                "functional-constructor-expr/error-constructor-expr/error_constructor_expr_assert_02.json");
        testFile("functional-constructor-expr/error-constructor-expr/error_constructor_expr_source_03.bal",
                "functional-constructor-expr/error-constructor-expr/error_constructor_expr_assert_03.json");
        testFile("functional-constructor-expr/error-constructor-expr/error_constructor_expr_source_04.bal",
                "functional-constructor-expr/error-constructor-expr/error_constructor_expr_assert_04.json");
    }

    @Test
    public void testMissingElements() {
        test("error(\"Test\";", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_missing_elements_01.json");
        test("error(\"ERROR_REASON\", message = \"error message\";", "functional-constructor-expr/error-constructor" +
                "-expr/error_constructor_expr_with_missing_elements_02.json");
        test("error\"ERROR_REASON\", message = \"error message\");", "functional-constructor-expr/error-constructor" +
                "-expr/error_constructor_expr_with_missing_elements_03.json");
        test("error\"ERROR_REASON\", message = \"error message\";", "functional-constructor-expr/error-constructor" +
                "-expr/error_constructor_expr_with_missing_elements_04.json");
        test("error", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_missing_elements_05.json");
    }

    @Test
    public void testExtraElements() {
        test("errorr(\"message\")", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_extra_elements_01.json");
        test("error((\"message\")", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_extra_elements_02.json");
        test("error&(\"message\")", "functional-constructor-expr/error-constructor-expr" +
                "/error_constructor_expr_with_extra_elements_03.json");
    }
}
