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
 * Test parsing documentations.
 * 
 * @since 1.3.0
 */
public class DocumentationTest extends AbstractMiscTest {

    // Valid syntax

    @Test
    public void testSimpleDocumentationLine() {
        testFile("documentation/doc_source_03.bal", "documentation/doc_assert_03.json");
    }

    @Test
    public void testSimpleParameterDocumentationLine() {
        testFile("documentation/doc_source_04.bal", "documentation/doc_assert_04.json");
    }

    @Test
    public void testSimpleReturnParameterDocumentationLine() {
        testFile("documentation/doc_source_05.bal", "documentation/doc_assert_05.json");
    }

    @Test
    public void testSimpleDeprecationDocumentationLine() {
        testFile("documentation/doc_source_06.bal", "documentation/doc_assert_06.json");
    }

    @Test
    public void testSimpleReferenceDocumentationLine() {
        testFile("documentation/doc_source_07.bal", "documentation/doc_assert_07.json");
    }

    @Test
    public void testDocumentation() {
        testFile("documentation/doc_source_01.bal", "documentation/doc_assert_01.json");
        testFile("documentation/doc_source_12.bal", "documentation/doc_assert_12.json");
    }

    @Test
    public void testRecordFieldDocumentation() {
        testFile("documentation/doc_source_02.bal", "documentation/doc_assert_02.json");
    }

    @Test
    public void testObjectFieldDocumentation() {
        testFile("documentation/doc_source_08.bal", "documentation/doc_assert_08.json");
    }

    @Test
    public void testServiceBodyBlockDocumentation() {
        testFile("documentation/doc_source_09.bal", "documentation/doc_assert_09.json");
    }

    @Test
    public void testTippleBacktickDocumentation() {
        testFile("documentation/doc_source_10.bal", "documentation/doc_assert_10.json");
    }

    @Test
    public void testDoubleBacktickDocumentation() {
        testFile("documentation/doc_source_11.bal", "documentation/doc_assert_11.json");
    }

    @Test
    public void testDocReferenceWithSpecialKeywords() {
        testFile("documentation/doc_source_14.bal", "documentation/doc_assert_14.json");
    }

    @Test
    public void testMultiLineDocumentation() {
        testFile("documentation/doc_source_15.bal", "documentation/doc_assert_15.json");
    }

    @Test
    public void testSingleBacktickContentInDocumentation() {
        testFile("documentation/doc_source_16.bal", "documentation/doc_assert_16.json");
    }

    @Test
    public void testBacktickContentForFunctionKeyword() {
        testFile("documentation/doc_source_17.bal", "documentation/doc_assert_17.json");
    }

    // Invalid Syntax

    @Test
    public void testInvalidDocumentation() {
        testFile("documentation/doc_source_13.bal", "documentation/doc_assert_13.json");
    }
}
