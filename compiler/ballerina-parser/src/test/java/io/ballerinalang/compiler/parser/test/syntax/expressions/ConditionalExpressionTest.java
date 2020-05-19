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
    public void testConditionalExpr() {
        test("a ?: b", "conditional-expr/conditional_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testConditionalElvisWithMissingExpression() {
        test("a ?: ;", "conditional-expr/conditional_assert_02.json");
        test("?: b;", "conditional-expr/conditional_assert_03.json");
    }
}
