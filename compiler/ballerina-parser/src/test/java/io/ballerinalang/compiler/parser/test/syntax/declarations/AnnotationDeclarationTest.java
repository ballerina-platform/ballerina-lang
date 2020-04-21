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
 * Test parsing annotation declarations.
 * 
 * @since 1.3.0
 */
public class AnnotationDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testAnnotDeclWithoutAttachPoints() {
        testFile("annot-decl/annot_decl_source_01.bal", "annot-decl/annot_decl_assert_01.json");
    }

    @Test
    public void testAnnotDeclWithAttachPoints() {
        testFile("annot-decl/annot_decl_source_02.bal", "annot-decl/annot_decl_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testAnnotTag() {
        testFile("annot-decl/annot_decl_source_03.bal", "annot-decl/annot_decl_assert_03.json");
    }

    @Test
    public void testMissingOnKeyword() {
        testFile("annot-decl/annot_decl_source_04.bal", "annot-decl/annot_decl_assert_04.json");
    }

    @Test
    public void testMissingSourceKeywordForSouceOnlyIdent() {
        testFile("annot-decl/annot_decl_source_05.bal", "annot-decl/annot_decl_assert_05.json");
    }

    @Test
    public void testMissingSecondIdent() {
        testFile("annot-decl/annot_decl_source_06.bal", "annot-decl/annot_decl_assert_06.json");
    }

    @Test
    public void testMissingCommaBetweenIdents() {
        testFile("annot-decl/annot_decl_source_07.bal", "annot-decl/annot_decl_assert_07.json");
    }

    @Test
    public void testAdditionalTokens() {
        testFile("annot-decl/annot_decl_source_08.bal", "annot-decl/annot_decl_assert_08.json");
    }

    @Test
    public void testMissingSemicolon() {
        testFile("annot-decl/annot_decl_source_09.bal", "annot-decl/annot_decl_assert_09.json");
    }
    
    @Test
    public void testInvalidAttachPointIdent() {
        testFile("annot-decl/annot_decl_source_10.bal", "annot-decl/annot_decl_assert_10.json");
    }
}
