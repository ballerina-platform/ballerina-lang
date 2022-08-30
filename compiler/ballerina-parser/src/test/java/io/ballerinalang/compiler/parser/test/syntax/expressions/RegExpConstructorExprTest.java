/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test parsing regexp-constructor-expr.
 *
 * @since 2201.3.0
 */
public class RegExpConstructorExprTest extends AbstractExpressionsTest {
    // Valid source tests

    @Test
    public void testRegExpWithAssertions() {
        testFile("regexp-constructor-expr/regexp_constructor_source_01.bal",
                "regexp-constructor-expr/regexp_constructor_assert_01.json");
    }

    @Test
    public void testRegExpWithPipedSequences() {
        testFile("regexp-constructor-expr/regexp_constructor_source_02.bal",
                "regexp-constructor-expr/regexp_constructor_assert_02.json");
    }

    @Test
    public void testRegExpWithQualifiers() {
        testFile("regexp-constructor-expr/regexp_constructor_source_03.bal",
                "regexp-constructor-expr/regexp_constructor_assert_03.json");
    }

    @Test
    public void testRegExpWithQualifiers2() {
        testFile("regexp-constructor-expr/regexp_constructor_source_04.bal",
                "regexp-constructor-expr/regexp_constructor_assert_04.json");
    }

    @Test
    public void testRegExpWithReAtomWithDot() {
        testFile("regexp-constructor-expr/regexp_constructor_source_05.bal",
                "regexp-constructor-expr/regexp_constructor_assert_05.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtom() {
        testFile("regexp-constructor-expr/regexp_constructor_source_06.bal",
                "regexp-constructor-expr/regexp_constructor_assert_06.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithNumericEscape() {
        testFile("regexp-constructor-expr/regexp_constructor_source_07.bal",
                "regexp-constructor-expr/regexp_constructor_assert_07.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithControlEscape() {
        testFile("regexp-constructor-expr/regexp_constructor_source_08.bal",
                "regexp-constructor-expr/regexp_constructor_assert_08.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithQuoteEscape() {
        testFile("regexp-constructor-expr/regexp_constructor_source_09.bal",
                "regexp-constructor-expr/regexp_constructor_assert_09.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithUnicodePropertyEscape() {
        testFile("regexp-constructor-expr/regexp_constructor_source_10.bal",
                "regexp-constructor-expr/regexp_constructor_assert_10.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithSimpleCharClassEscape() {
        testFile("regexp-constructor-expr/regexp_constructor_source_11.bal",
                "regexp-constructor-expr/regexp_constructor_assert_11.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetAtomWithEscapedDash() {
        testFile("regexp-constructor-expr/regexp_constructor_source_12.bal",
                "regexp-constructor-expr/regexp_constructor_assert_12.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetRange() {
        testFile("regexp-constructor-expr/regexp_constructor_source_13.bal",
                "regexp-constructor-expr/regexp_constructor_assert_13.json");
    }

    @Test
    public void testRegExpWithReAtomWithCharSetNoDash() {
        testFile("regexp-constructor-expr/regexp_constructor_source_14.bal",
                "regexp-constructor-expr/regexp_constructor_assert_14.json");
    }

    @Test
    public void testRegExpWithReAtomWithoutFlags() {
        testFile("regexp-constructor-expr/regexp_constructor_source_15.bal",
                "regexp-constructor-expr/regexp_constructor_assert_15.json");
    }

    @Test
    public void testRegExpWithReAtomWithFlags() {
        testFile("regexp-constructor-expr/regexp_constructor_source_16.bal",
                "regexp-constructor-expr/regexp_constructor_assert_16.json");
    }

    @Test
    public void testComplexRegExp() {
        testFile("regexp-constructor-expr/regexp_constructor_source_17.bal",
                "regexp-constructor-expr/regexp_constructor_assert_17.json");
    }

    // Recovery tests

    @Test
    public void testRecoveryWithInvalidQuantifierWithReAssertion() {
        testFile("regexp-constructor-expr/regexp_constructor_source_18.bal",
                "regexp-constructor-expr/regexp_constructor_assert_18.json");
    }

    @Test
    public void testRecoveryWithInvalidQuantifier() {
        testFile("regexp-constructor-expr/regexp_constructor_source_19.bal",
                "regexp-constructor-expr/regexp_constructor_assert_19.json");
    }

    @Test
    public void testRecoveryWithInvalidFlags() {
        testFile("regexp-constructor-expr/regexp_constructor_source_20.bal",
                "regexp-constructor-expr/regexp_constructor_assert_20.json");
    }

    @Test
    public void testRecoveryWithInvalidUnicodeProperty() {
        testFile("regexp-constructor-expr/regexp_constructor_source_21.bal",
                "regexp-constructor-expr/regexp_constructor_assert_21.json");
    }

    @Test
    public void testRecoveryWithInvalidUnicodeProperty2() {
        testFile("regexp-constructor-expr/regexp_constructor_source_22.bal",
                "regexp-constructor-expr/regexp_constructor_assert_22.json");
    }

    @Test
    public void testRecoveryWithInvalidUnicodePropertyValueChar() {
        testFile("regexp-constructor-expr/regexp_constructor_source_23.bal",
                "regexp-constructor-expr/regexp_constructor_assert_23.json");
    }

    @Test
    public void testRecoveryWithInvalidCapturingGroup() {
        testFile("regexp-constructor-expr/regexp_constructor_source_24.bal",
                "regexp-constructor-expr/regexp_constructor_assert_24.json");
    }

    @Test
    public void testRecoveryWithInvalidCharClass() {
        testFile("regexp-constructor-expr/regexp_constructor_source_25.bal",
                "regexp-constructor-expr/regexp_constructor_assert_25.json");
    }
}
