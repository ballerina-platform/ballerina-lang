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
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test parsing quoted identifier literals.
 *
 */
public class QuotedIdentifierTest extends AbstractMiscTest {

    // Valid syntax

    @Test
    public void testAlphanumericIdentifier() {
        testFile("quoted-identifiers/alphanumeric_identifier_source.bal",
                "quoted-identifiers/alphanumeric_identifier_assert.json");
    }

    @Test
    public void testSpecialCharacterIdentifier() {
        testFile("quoted-identifiers/special_char_identifier_source.bal",
                "quoted-identifiers/special_char_identifier_assert.json");
    }

    @Test
    public void testUnicodeCharacterIdentifier() {
        testFile("quoted-identifiers/unicode_char_identifier_source.bal",
                "quoted-identifiers/unicode_char_identifier_assert.json");
    }

    @Test
    public void testUnicodeCodePointIdentifier() {
        testFile("quoted-identifiers/unicode_codepoint_source.bal", "quoted-identifiers/unicode_codepoint_assert.json");
    }

    // Invalid Syntax

    @Test
    public void testInvalidQuotedIdentifier() {
        testFile("quoted-identifiers/invalid_identifier_source_01.bal",
                "quoted-identifiers/invalid_identifier_assert_01.json");
    }

    @Test
    public void testQuotedIdentifierWithInvalidEscapes() {
        testFile("quoted-identifiers/invalid_identifier_source_02.bal",
                "quoted-identifiers/invalid_identifier_assert_02.json");
    }
}
