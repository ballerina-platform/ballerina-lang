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
 * Test parsing list constructor expression.
 */
public class ListConstructorTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testEmptyList() {
        test("[]", "list-constructor/list_constructor_assert_01.json");
    }

    @Test
    public void testExpressionList() {
        test("[ a, a+b, !(a+c), true, +d ]", "list-constructor/list_constructor_assert_02.json");
    }

    @Test
    public void testListRecursively() {
        test("[ [[]], [ a, b ], { a:b, c:[ d, { e:f } ] } ]", "list-constructor/list_constructor_assert_03.json");
    }

    // Recovery tests

    @Test
    public void testListWithMissingCloseBracket() {
        test("[", "list-constructor/list_constructor_assert_04.json");
        test("[ a, b + c ", "list-constructor/list_constructor_assert_05.json");
    }

    @Test
    public void testListWithMissingOpenBracket() {
        test("]", "list-constructor/list_constructor_assert_06.json");
    }

    @Test
    public void testListWithOnlyCommas() {
        test("[,,]", "list-constructor/list_constructor_assert_07.json");
    }

    @Test
    public void testListWithMissingCommas() {
        test("[a b c]", "list-constructor/list_constructor_assert_08.json");
    }

    @Test
    public void testListWithInvalidAnnotations() {
        test("@Annotation [a + b, c]", "list-constructor/list_constructor_assert_09.json");
    }
}
