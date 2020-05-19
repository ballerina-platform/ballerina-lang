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
 * Test parsing optional field access expression.
 */
public class OptionalFieldAccessTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testOptionalFieldAccess() {
        test("expression ?. name", "optional-field-access/optional_field_access_assert_01.json");
    }

    @Test
    public void testOptionalFieldAccessWithOpPrecedence() {
        test("a + b ?. name + c", "optional-field-access/optional_field_access_assert_02.json");
    }

    // Recovery test

    @Test
    public void testOptionalFieldAccessWithMissingFieldName() {
        test("expression ?. ;", "optional-field-access/optional_field_access_assert_03.json");
    }

    @Test
    public void testOptionalFieldAccessWithMissingExpression() {
        test("?. name;", "optional-field-access/optional_field_access_assert_04.json");
        test("{foo : ?. name };", "optional-field-access/optional_field_access_assert_05.json");
        test("[foo, ?. name];", "optional-field-access/optional_field_access_assert_06.json");
        test("let int a = ?. name in c;", "optional-field-access/optional_field_access_assert_07.json");
        test("from int a in b where ?. select ?. ;", "optional-field-access/optional_field_access_assert_08.json");
    }
}
