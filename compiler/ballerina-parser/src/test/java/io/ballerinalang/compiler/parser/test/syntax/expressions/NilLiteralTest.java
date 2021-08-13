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
 * Test parsing nil literal.
 */
public class NilLiteralTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testSimpleNilLiteral() {
        test("()", "basic-literals/nil_literal_assert_01.json");
        test("null", "basic-literals/nil_literal_assert_02.json");
    }

    @Test
    public void testNilLiteral() {
        test("() != null + a", "basic-literals/nil_literal_assert_03.json");
        test("((null) + (()))", "basic-literals/nil_literal_assert_04.json");
        testFile("basic-literals/nil_literal_source_01.bal", "basic-literals/nil_literal_assert_05.json");
        testFile("basic-literals/nil_literal_source_02.bal", "basic-literals/nil_literal_assert_06.json");
        testFile("basic-literals/nil_literal_source_03.bal", "basic-literals/nil_literal_assert_07.json");
        testFile("basic-literals/nil_literal_source_04.bal", "basic-literals/nil_literal_assert_08.json");
    }

    @Test
    public void testNilLiteralInMatchGuard() {
        testFile("basic-literals/nil_literal_source_11.bal", "basic-literals/nil_literal_assert_11.json");
    }

    // Recovery test

    @Test
    public void testNilLiteralWithMissingOpenParenthesis() {
        test(")", "basic-literals/nil_literal_assert_09.json");
        testFile("basic-literals/nil_literal_source_05.bal", "basic-literals/nil_literal_assert_10.json");
    }
}
