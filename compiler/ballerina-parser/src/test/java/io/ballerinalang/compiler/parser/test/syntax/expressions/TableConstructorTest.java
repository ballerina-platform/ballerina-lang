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
 * Test parsing table constructor expression.
 */
public class TableConstructorTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testSimplestTableForm() {
        test("table []", "table-constructor/table_constructor_assert_01.json");
    }

    @Test
    public void testTableWithOneRow() {
        test("table [{\"name\":\"Lochana\", age:24}]",
                "table-constructor/table_constructor_assert_02.json");
    }

    @Test
    public void testTableWithMultipleRows() {
        test("table [{\"name\":\"Lochana\", age:24}, {\"name\":\"Dulmina\", age:25}]",
                "table-constructor/table_constructor_assert_03.json");
    }

    @Test
    public void testTableWithKeySpecifier() {
        test("table key(age) []", "table-constructor/table_constructor_assert_04.json");
        test("table key() [{age:24},{age:25}]", "table-constructor/table_constructor_assert_05.json");
        test("table key(age,name) [{age:24}]", "table-constructor/table_constructor_assert_06.json");
        test("table key(email, bu) [\n" +
                        "   { email: \"supuns@wso2.com\", bu: \"IPaaS\" },\n" +
                        "   { email: \"lochanaj@wso2.com\", bu: \"IPaaS\" }\n" +
                        "]",
                "table-constructor/table_constructor_assert_07.json");
    }
}
