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
 * Test parsing quoted string literals.
 * 
 * @since 1.3.0
 */
public class QuotedStringLiteralTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testQuotedStringLiteral() {
        test("\"hello \\u{A}world \\n how \\tare you?\"", "basic-literals/quoted_string_literal_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testQuotedWithMissingEndingQuote() {
        testFile("basic-literals/quoted_string_literal_source_02.bal",
                "basic-literals/quoted_string_literal_assert_02.json");
    }

    @Test
    public void testQuotedWithInvalidEscapeSequence() {
        test("\"hello \\u{K} world \\h how are you?\"", "basic-literals/quoted_string_literal_assert_03.json");
        test("\"\\Ballerina\"", "basic-literals/quoted_string_literal_assert_04.json");
    }
}
