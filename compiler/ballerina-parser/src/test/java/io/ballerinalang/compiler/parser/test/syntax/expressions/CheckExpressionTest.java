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

    // Recovery test

    @Test
    public void testCheckWithMissingExpr() {
        testFile("check-expr/check_expr_source_03.bal", "check-expr/check_expr_assert_03.json");
    }

    @Test
    public void testCheckWithMissingBinaryOp() {
        testFile("check-expr/check_expr_source_04.bal", "check-expr/check_expr_assert_04.json");
    }
}
