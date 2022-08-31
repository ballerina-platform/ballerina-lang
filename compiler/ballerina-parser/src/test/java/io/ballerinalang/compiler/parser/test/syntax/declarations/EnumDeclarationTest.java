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
 * Test parsing enum declarations.
 */
public class EnumDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests
    @Test
    public void testSimpleFuncDef() {
        testFile("enum-decl/enum_decl_source_01.bal", "enum-decl/enum_decl_assert_01.json");
    }

    @Test
    public void testOptionalSemicolon() {
        testFile("enum-decl/enum_decl_source_10.bal", "enum-decl/enum_decl_assert_10.json");
    }

    //Recovery tests
    @Test
    public void testInvalidEnumMissingEnumKeyword() {
        testFile("enum-decl/enum_decl_source_02.bal", "enum-decl/enum_decl_assert_02.json");
    }

    @Test
    public void testInvalidEnumMissingEnumNameIdentifier() {
        testFile("enum-decl/enum_decl_source_03.bal", "enum-decl/enum_decl_assert_03.json");
    }

    @Test
    public void testInvalidEnumMissingComma() {
        testFile("enum-decl/enum_decl_source_04.bal", "enum-decl/enum_decl_assert_04.json");
    }

    @Test
    public void testInvalidEnumMissingMemberName() {
        testFile("enum-decl/enum_decl_source_05.bal", "enum-decl/enum_decl_assert_05.json");
    }

    @Test
    public void testInvalidEnumEmptyMemberList() {
        testFile("enum-decl/enum_decl_source_06.bal", "enum-decl/enum_decl_assert_06.json");
    }

    @Test
    public void testInvalidEnumMissingEqualInMemberRhs() {
        testFile("enum-decl/enum_decl_source_07.bal", "enum-decl/enum_decl_assert_07.json");
    }

    @Test
    public void testInvalidEnumMultipleExtraTokens() {
        testFile("enum-decl/enum_decl_source_08.bal", "enum-decl/enum_decl_assert_08.json");
    }

    @Test
    public void testInvalidEnumMultipleTokensMissing() {
        testFile("enum-decl/enum_decl_source_09.bal", "enum-decl/enum_decl_assert_09.json");
    }

}
