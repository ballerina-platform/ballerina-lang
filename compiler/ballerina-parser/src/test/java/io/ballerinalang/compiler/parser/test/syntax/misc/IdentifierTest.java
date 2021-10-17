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
public class IdentifierTest extends AbstractMiscTest {

    // Valid syntax

    @Test
    public void testAlphanumericQuotedIdentifier() {
        testFile("identifiers/alphanumeric_identifier_source.bal",
                "identifiers/alphanumeric_identifier_assert.json");
    }

    @Test
    public void testSpecialCharacterIdentifier() {
        testFile("identifiers/special_char_identifier_source.bal",
                "identifiers/special_char_identifier_assert.json");
    }

    @Test
    public void testUnicodeCharacterIdentifier() {
        testFile("identifiers/unicode_char_identifier_source.bal",
                "identifiers/unicode_char_identifier_assert.json");
    }

    @Test
    public void testUnicodeCodePointIdentifier() {
        testFile("identifiers/unicode_codepoint_source.bal", "identifiers/unicode_codepoint_assert.json");
    }

    @Test
    public void testKeywordsAsIdentifiers() {
        testFile("identifiers/keywords_as_identifiers_source.bal",
                "identifiers/keywords_as_identifiers_assert.json");
    }

    // Invalid Syntax

    @Test
    public void testInvalidQuotedIdentifier() {
        testFile("identifiers/invalid_identifier_source_01.bal",
                "identifiers/invalid_identifier_assert_01.json");
    }

    @Test
    public void testIdentifierWithInvalidEscapes() {
        testFile("identifiers/invalid_identifier_source_02.bal",
                "identifiers/invalid_identifier_assert_02.json");
    }

    @Test
    public void testIncompleteQuotedIdentifier() {
        testFile("identifiers/incomplete_identifier_source.bal",
                "identifiers/incomplete_identifier_assert.json");
    }
}
