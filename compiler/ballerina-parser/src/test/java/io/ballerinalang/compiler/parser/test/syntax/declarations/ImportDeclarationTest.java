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
 * Test parsing import declarations.
 * 
 * @since 1.3.0
 */
public class ImportDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testSimpleImport() {
        test("import-decl/import_decl_source_01.bal", "import-decl/import_decl_assert_01.json");
    }

    @Test
    public void testImportWithComplexModuleName() {
        test("import-decl/import_decl_source_02.bal", "import-decl/import_decl_assert_02.json");
    }

    @Test
    public void testImportWithOrgName() {
        test("import-decl/import_decl_source_03.bal", "import-decl/import_decl_assert_03.json");
    }

    @Test
    public void testImportWithMajorVersion() {
        test("import-decl/import_decl_source_04.bal", "import-decl/import_decl_assert_04.json");
    }

    @Test
    public void testImportWithMinorVersion() {
        test("import-decl/import_decl_source_05.bal", "import-decl/import_decl_assert_05.json");
    }

    @Test
    public void testImportWithPatchVersion() {
        test("import-decl/import_decl_source_06.bal", "import-decl/import_decl_assert_06.json");
    }

    @Test
    public void testImportWithPrefix() {
        test("import-decl/import_decl_source_07.bal", "import-decl/import_decl_assert_07.json");
    }

    @Test
    public void testImportWithVersionAndPrefix() {
        test("import-decl/import_decl_source_08.bal", "import-decl/import_decl_assert_08.json");
    }

    // Recovery tests

    @Test
    public void testSimpleImportWithMissingModuleName() {
        test("import-decl/import_decl_source_09.bal", "import-decl/import_decl_assert_09.json");
    }

    @Test
    public void testImportKeywordOnly() {
        test("import-decl/import_decl_source_10.bal", "import-decl/import_decl_assert_10.json");
    }

    @Test
    public void testSimpleImportMissingSemicolon() {
        test("import-decl/import_decl_source_11.bal", "import-decl/import_decl_assert_11.json");
    }

    @Test
    public void testMissingSemicolonWithOrgNameAndModuleName() {
        test("import-decl/import_decl_source_12.bal", "import-decl/import_decl_assert_12.json");
    }

    @Test
    public void testMissingSemicolonAndModuleName() {
        test("import-decl/import_decl_source_13.bal", "import-decl/import_decl_assert_13.json");
    }

    @Test
    public void testMissingSemicolonAndVersionNumber() {
        testFile("import-decl/import_decl_source_14.bal", "import-decl/import_decl_assert_14.json");
    }

    @Test
    public void testMissingDotInModuleName() {
        test("import-decl/import_decl_source_15.bal", "import-decl/import_decl_assert_15.json");
    }

    @Test
    public void testMissionVersionKeyword() {
        test("import-decl/import_decl_source_16.bal", "import-decl/import_decl_assert_16.json");
    }

    @Test
    public void testInvalidTokenAfterModuleName() {
        test("import-decl/import_decl_source_17.bal", "import-decl/import_decl_assert_17.json");
    }

    @Test
    public void testDotsAfterModuleName() {
        test("import-decl/import_decl_source_18.bal", "import-decl/import_decl_assert_18.json");
    }

    @Test
    public void testMissingVersionKeyWordAndAsKeyword() {
        test("import-decl/import_decl_source_19.bal", "import-decl/import_decl_assert_19.json");
    }

    @Test
    public void testInvalidTokenAfterOrgName() {
        test("import-decl/import_decl_source_20.bal", "import-decl/import_decl_assert_20.json");
    }

    @Test
    public void testInvalidTokensEverywhere() {
        test("import-decl/import_decl_source_21.bal", "import-decl/import_decl_assert_21.json");
    }

    @Test
    public void testOutOfOrderImport() {
        testFile("import-decl/import_decl_source_22.bal", "import-decl/import_decl_assert_22.json");
    }

    @Test
    public void testOutOfOrderImports() {
        testFile("import-decl/import_decl_source_23.bal", "import-decl/import_decl_assert_23.json");
    }
}
