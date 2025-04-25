/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 * Test parsing mapping constructor expression.
 *
 * @since 2201.13`
 */
public class NaturalExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testNaturalExpression() {
        testFile("natural-expr/natural_expr_source_01.bal", "natural-expr/natural_expr_assert_01.json");
    }

    // Invalid syntax
    @Test
    public void testInvalidNaturalExpression() {
        testFile("natural-expr/natural_expr_source_02.bal", "natural-expr/natural_expr_assert_02.json");
    }
}
