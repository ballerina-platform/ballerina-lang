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
 * Test parsing service constructor expression.
 */
public class ServiceConstructorExpressionTest extends AbstractExpressionsTest {

    @Test
    public void testServiceConstructorExpr() {
        testFile("service-constructor-expression/service_constructor_expr_source_01.bal",
             "service-constructor-expression/service_constructor_expr_assert_01.json");
    }

    @Test
    public void testServiceConstructorExprWithNestedComponents() {
        testFile("service-constructor-expression/service_constructor_expr_source_02.bal",
            "service-constructor-expression/service_constructor_expr_assert_02.json");
    }

    // Recovery test

    @Test
    public void testServiceConstructorExprNestedRecovery() {
        testFile("service-constructor-expression/service_constructor_expr_source_03.bal",
            "service-constructor-expression/service_constructor_expr_assert_03.json");
    }

    @Test
    public void testServiceConstructorWithMissingCloseBrace() {
        testFile("service-constructor-expression/service_constructor_expr_source_04.bal",
            "service-constructor-expression/service_constructor_expr_assert_04.json");
    }
}
