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
 * Test parsing quoted identifier literals.
 *
 */
public class IdentifierLiteralTest extends AbstractExpressionsTest {

    @Test
    public void testAlphanumericIdentifier() {
        testFile("identifier-literal/alphanumeric_identifier_source.bal",
                "identifier-literal/alphanumeric_identifier_assert.json");
    }

    @Test
    public void testeSpecialCharacterIdentifier() {
        testFile("identifier-literal/special_char_identifier_source.bal",
                "identifier-literal/special_char_identifier_assert.json");
    }

    @Test
    public void testeUnicodeCharacterIdentifier() {
        testFile("identifier-literal/unicode_char_identifier_source.bal",
                "identifier-literal/unicode_char_identifier_assert.json");
    }

    @Test
    public void testeInvalidCharacterIdentifier() {
        testFile("identifier-literal/invalid_identifier_source.bal",
                "identifier-literal/invalid_identifier_assert.json");
    }


}
