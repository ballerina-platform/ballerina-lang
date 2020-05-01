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
    public void testRecoveryInsideNextedInterpolation() {
        testFile("xml-template/xml_template_source_08.bal", "xml-template/xml_template_assert_08.json");
    }
}
