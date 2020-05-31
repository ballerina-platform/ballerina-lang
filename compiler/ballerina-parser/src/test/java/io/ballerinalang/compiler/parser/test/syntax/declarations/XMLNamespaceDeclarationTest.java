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
package io.ballerinalang.compiler.parser.test.syntax.declarations;

import org.testng.annotations.Test;

/**
 * Test parsing XML namespace declarations.
 * 
 * @since 1.3.0
 */
public class XMLNamespaceDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testXMLNamespaceDecl() {
        testFile("xmlns-decl/xmlns_decl_source_01.bal", "xmlns-decl/xmlns_decl_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testMissingSemicolon() {
        testFile("xmlns-decl/xmlns_decl_source_02.bal", "xmlns-decl/xmlns_decl_assert_02.json");
    }

    @Test
    public void testMissingNamespacePrefix() {
        testFile("xmlns-decl/xmlns_decl_source_03.bal", "xmlns-decl/xmlns_decl_assert_03.json");
    }

    @Test
    public void testMissingAsKeyword() {
        testFile("xmlns-decl/xmlns_decl_source_04.bal", "xmlns-decl/xmlns_decl_assert_04.json");
    }

    @Test
    public void testMissingXMLNSKeyword() {
        testFile("xmlns-decl/xmlns_decl_source_05.bal", "xmlns-decl/xmlns_decl_assert_05.json");
    }

    @Test
    public void testMissingNamespaceURI() {
        testFile("xmlns-decl/xmlns_decl_source_06.bal", "xmlns-decl/xmlns_decl_assert_06.json");
    }

    @Test
    public void testAdditionalTokens() {
        testFile("xmlns-decl/xmlns_decl_source_07.bal", "xmlns-decl/xmlns_decl_assert_07.json");
    }

    @Test
    public void testComplexExpressionsForNSUri() {
        testFile("xmlns-decl/xmlns_decl_source_08.bal", "xmlns-decl/xmlns_decl_assert_08.json");
    }
}
