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
 * Test parsing binary expressions.
 */
public class FuncCallExpressionTest extends AbstractExpressionsTest {

    @Test
    public void testEmptyFuncCall() {
        test("foo()", "func-call/func_call_assert_01.json");
    }

    @Test
    public void testFuncCallWithArgs() {
        test("foo(5, a, age = 18, ...subjects)", "func-call/func_call_assert_02.json");
    }

    @Test
    public void testFuncCallWithPositionalArgsOnly() {
        test("foo(5, true)", "func-call/func_call_assert_03.json");
    }

    @Test
    public void testFuncCallWithNamedArgsOnly() {
        test("foo(age = 18)", "func-call/func_call_assert_04.json");
    }

    @Test
    public void testFuncCallWithRestArgsOnly() {
        test("foo(...subjects)", "func-call/func_call_assert_05.json");
    }

    @Test
    public void testNestedFuncCalls() {
        test("foo( bar(5, a, age = baz(), ...subjects), 5, a, age = 18, ...subjects)",
                "func-call/func_call_assert_10.json");
    }

    @Test
    public void testBracedExprAsArgs() {
        testFile("func-call/func_call_source_12.bal", "func-call/func_call_assert_12.json");
    }

    // Recovery tests

    @Test
    public void testMissingClosinParen() {
        test("foo(", "func-call/func_call_assert_06.json");
    }

    @Test
    public void testMissingClosinParenWithArgs() {
        test("foo(5, a, age = 18, ...subjects", "func-call/func_call_assert_07.json");
    }

    @Test
    public void testMissingCommaInArgs() {
        test("foo(5, a age = 18, ...subjects)", "func-call/func_call_assert_08.json");
    }

    @Test
    public void testAdditionalCommaInArgs() {
        test("foo(5, a, age = 18,)", "func-call/func_call_assert_09.json");
    }

    @Test
    public void testNestedFuncCallsWithMissingComma() {
        test("foo( bar(5, a, age  baz() ...subjects), 5, a, age = 18, ...subjects)",
                "func-call/func_call_assert_11.json");
    }
}
