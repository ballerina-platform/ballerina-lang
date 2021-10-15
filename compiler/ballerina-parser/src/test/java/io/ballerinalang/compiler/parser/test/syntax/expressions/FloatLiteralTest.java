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
 * Test parsing floating point literal.
 */
public class FloatLiteralTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testDecimalFloatingPointNumber() {

        // DecimalNumber Exponent [FloatingPointTypeSuffix]
        testFile("basic-literals/float_literal_source_01.bal", "basic-literals/float_literal_assert_01.json");

        // DottedDecimalNumber [Exponent] [FloatingPointTypeSuffix]
        testFile("basic-literals/float_literal_source_02.bal", "basic-literals/float_literal_assert_02.json");

        // DecimalNumber FloatingPointTypeSuffix
        testFile("basic-literals/float_literal_source_03.bal", "basic-literals/float_literal_assert_03.json");
    }

    @Test
    public void testHexFloatingPointLiteral() {

        // HexIndicator HexNumber HexExponent
        testFile("basic-literals/float_literal_source_04.bal", "basic-literals/float_literal_assert_04.json");

        // HexIndicator DottedHexNumber [HexExponent]
        testFile("basic-literals/float_literal_source_05.bal", "basic-literals/float_literal_assert_05.json");
    }

    // Invalid syntax tests

    @Test
    public void testInvalidTrailingDot() {

        // DecimalFloatingPointNumber
        testFile("basic-literals/float_literal_source_06.bal", "basic-literals/float_literal_assert_06.json");

        // HexFloatingPointLiteral
        testFile("basic-literals/float_literal_source_07.bal", "basic-literals/float_literal_assert_07.json");
    }

    @Test
    public void testInvalidFloatingPointLiteral() {
        testFile("basic-literals/float_literal_source_08.bal", "basic-literals/float_literal_assert_08.json");
    }
}
