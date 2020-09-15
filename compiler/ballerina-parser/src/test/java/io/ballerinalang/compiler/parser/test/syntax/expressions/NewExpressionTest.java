/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing <code>new-expr</code>.
 */
public class NewExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testImplicitNewWithoutArgs() {
        test("new();", "new-expr/implicit-new-without-args.json");
    }

    @Test
    public void testImplicitNewWithArgs() {
        test("new(a)", "new-expr/implicit-new-with-1-arg.json");
    }

    @Test
    public void testImplicitNewWithMultipleArgs() {
        test("new(a, 10, \"12\")", "new-expr/implicit-new-with-multiple-args.json");
    }

    @Test
    public void testExplicitNewWithIdentifierWithoutArgs() {
        test("new Foo()", "new-expr/explicit-new-with-identifier-without-args.json");
    }

    @Test
    public void testExplicitNewWithIdentifierAndOneArg() {
        test("new Foo(10)", "new-expr/explicit-new-with-identifier-with-one-args.json");
    }

    @Test
    public void testExplicitNewWithIdentifierAndMultipleArgs() {
        test("new Foo(10, a, \"a\")", "new-expr/explicit-new-with-identifier-with-multiple-args.json");
    }

    @Test
    public void testExplicitNewWithObjectTypeDescriptorWithoutArgs() {
        testFile("new-expr/explicit-new-with-object-keyword-with-no-args.bal",
                "new-expr/explicit-new-with-object-keyword-with-no-args.json");
    }

    @Test
    public void testExplicitNewWithObjectTypeDescriptorWithOneArg() {
        testFile("new-expr/explicit-new-with-object-keyword-with-one-args.bal",
                "new-expr/explicit-new-with-object-keyword-with-one-args.json");
    }

    @Test
    public void testExplicitNewWithObjectTypeDescriptorWithMultipleArgs() {
        testFile("new-expr/explicit-new-with-object-keyword-with-multiple-args.bal",
                "new-expr/explicit-new-with-object-keyword-with-multiple-args.json");
    }

    // Invalid syntax

    @Test
    public void testImplicitNewWithoutArgsNegative() {
        test("new);", "new-expr/implicit-new-without-args-negative01.json");
        test("new(;", "new-expr/implicit-new-without-args-negative02.json");
    }

    @Test
    public void testImplicitNewWithArgsNegative() {
        test("new a)", "new-expr/implicit-new-with-args-negative01.json");
        test("new(a", "new-expr/implicit-new-with-args-negative02.json");
        test("new(a, b", "new-expr/implicit-new-with-args-negative03.json");
        test("new a, b)", "new-expr/implicit-new-with-args-negative04.json");
    }

    @Test
    public void testExplicitNewWithIdentifierWithoutArgsNegative() {
        test("new Foo)", "new-expr/explicit-new-with-identifier-without-args-negative01.json");
        test("new Foo(", "new-expr/explicit-new-with-identifier-without-args-negative02.json");
    }

    @Test
    public void testExplicitNewWithIdentifierWithArgsNegative() {
        test("new Foo 1, a)", "new-expr/explicit-new-with-identifier-with-args-negative01.json");
        test("new Foo(1, a", "new-expr/explicit-new-with-identifier-with-args-negative02.json");
    }

    @Test
    public void testExplicitNewWithObjectTypeDescriptorWithOneArgNegative() {
        testFile("new-expr/explicit-new-with-object-keyword-with-one-arg-negative01.bal",
                "new-expr/explicit-new-with-object-keyword-with-one-arg-negative01.json");
        testFile("new-expr/explicit-new-with-object-keyword-with-one-arg-negative02.bal",
                "new-expr/explicit-new-with-object-keyword-with-one-arg-negative02.json");
    }

    @Test
    public void testExplicitNewWithObjectTypeDescriptorWithMultipleArgsNegative() {
        testFile("new-expr/explicit-new-with-object-keyword-with-multiple-args-negative01.bal",
                "new-expr/explicit-new-with-object-keyword-with-multiple-args-negative01.json");
        testFile("new-expr/explicit-new-with-object-keyword-with-multiple-args-negative02.bal",
                "new-expr/explicit-new-with-object-keyword-with-multiple-args-negative02.json");
    }
}
