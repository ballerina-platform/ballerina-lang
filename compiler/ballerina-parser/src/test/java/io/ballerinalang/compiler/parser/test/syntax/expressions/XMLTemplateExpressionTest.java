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
 * Test parsing XML template expression.
 * 
 * @since 2.0.0
 */
public class XMLTemplateExpressionTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testBasicXML() {
        testFile("xml-template/xml_template_source_01.bal", "xml-template/xml_template_assert_01.json");
    }

    @Test
    public void testXMLWithAttributes() {
        testFile("xml-template/xml_template_source_02.bal", "xml-template/xml_template_assert_02.json");
    }

    @Test
    public void testDifferentQuotesInAttributeValue() {
        testFile("xml-template/xml_template_source_06.bal", "xml-template/xml_template_assert_06.json");
    }

    @Test
    public void testInterpolation() {
        testFile("xml-template/xml_template_source_07.bal", "xml-template/xml_template_assert_07.json");
    }

    @Test
    public void testEntityRefAndCharRefInAttributeValues() {
        testFile("xml-template/xml_template_source_11.bal", "xml-template/xml_template_assert_11.json");
    }

    @Test
    public void testXMLComment() {
        testFile("xml-template/xml_template_source_16.bal", "xml-template/xml_template_assert_16.json");
    }

    @Test
    public void testXMLPI() {
        testFile("xml-template/xml_template_source_21.bal", "xml-template/xml_template_assert_21.json");
    }

    @Test
    public void testComplexInterpolations() {
        testFile("xml-template/xml_template_source_23.bal", "xml-template/xml_template_assert_23.json");
    }

    @Test
    public void testInterpolationInComment() {
        testFile("xml-template/xml_template_source_25.bal", "xml-template/xml_template_assert_25.json");
    }

    @Test
    public void testInterpolationInPI() {
        testFile("xml-template/xml_template_source_26.bal", "xml-template/xml_template_assert_26.json");
    }

    @Test
    public void testWhitespaceAfterStartingBacktick() {
        testFile("xml-template/xml_template_source_30.bal", "xml-template/xml_template_assert_30.json");
    }

    // Recovery test

    @Test
    public void testMissingClosingAngleBracket() {
        testFile("xml-template/xml_template_source_03.bal", "xml-template/xml_template_assert_03.json");
    }

    @Test
    public void testMissingElementEndTag() {
        testFile("xml-template/xml_template_source_04.bal", "xml-template/xml_template_assert_04.json");
    }

    @Test
    public void testMissingClosingQuoteInAttributeValue() {
        testFile("xml-template/xml_template_source_05.bal", "xml-template/xml_template_assert_05.json");
    }

    @Test
    public void testRecoveryInsideNextedXMLTemplate() {
        testFile("xml-template/xml_template_source_08.bal", "xml-template/xml_template_assert_08.json");
    }

    @Test
    public void testInterpolationInsideElementTag() {
        testFile("xml-template/xml_template_source_09.bal", "xml-template/xml_template_assert_09.json");
    }

    @Test
    public void testInterpolatingTagName() {
        testFile("xml-template/xml_template_source_10.bal", "xml-template/xml_template_assert_10.json");
    }

    @Test
    public void testInvalidEntityRefAndCharRefInAttributeValues() {
        testFile("xml-template/xml_template_source_12.bal", "xml-template/xml_template_assert_12.json");
    }

    @Test
    public void testErrorsInAttributes() {
        testFile("xml-template/xml_template_source_13.bal", "xml-template/xml_template_assert_13.json");
    }

    @Test
    public void testRecoveryInsideInterpolation() {
        testFile("xml-template/xml_template_source_14.bal", "xml-template/xml_template_assert_14.json");
    }

    @Test
    public void testInvalidReferenceInText() {
        testFile("xml-template/xml_template_source_15.bal", "xml-template/xml_template_assert_15.json");
    }

    @Test
    public void testMissingTokenBeforeComment() {
        testFile("xml-template/xml_template_source_17.bal", "xml-template/xml_template_assert_17.json");
    }

    @Test
    public void testMissingCommentEnd() {
        testFile("xml-template/xml_template_source_18.bal", "xml-template/xml_template_assert_18.json");
    }

    @Test
    public void testDoubleHyphenInComments() {
        testFile("xml-template/xml_template_source_19.bal", "xml-template/xml_template_assert_19.json");
    }

    @Test
    public void testMissingOneHyphenInCommentStart() {
        testFile("xml-template/xml_template_source_20.bal", "xml-template/xml_template_assert_20.json");
    }

    @Test
    public void testErrorsInPI() {
        testFile("xml-template/xml_template_source_22.bal", "xml-template/xml_template_assert_22.json");
    }

    @Test
    public void testComplexInvalidInterpolations() {
        testFile("xml-template/xml_template_source_24.bal", "xml-template/xml_template_assert_24.json");
    }

    @Test
    public void testMissingClosingBacktick() {
        testFile("xml-template/xml_template_source_27.bal", "xml-template/xml_template_assert_27.json");
    }

    @Test
    public void testErrorBeforeTemplateExpression() {
        testFile("xml-template/xml_template_source_28.bal", "xml-template/xml_template_assert_28.json");
    }

    @Test
    public void testInvalidTokensInAttributeValue() {
        testFile("xml-template/xml_template_source_29.bal", "xml-template/xml_template_assert_29.json");
    }
}
