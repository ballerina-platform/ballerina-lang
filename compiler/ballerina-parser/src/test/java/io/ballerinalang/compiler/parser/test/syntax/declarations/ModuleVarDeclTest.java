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
 * Test parsing module var-decl, const-decl and listener-decl.
 * 
 * @since 1.3.0
 */
public class ModuleVarDeclTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testSimpleModuleVarDecl() {
        testFile("module-var-decl/module_var_decl_source_01.bal", "module-var-decl/module_var_decl_assert_01.json");
    }

    @Test
    public void testFinalVarDecl() {
        testFile("module-var-decl/module_var_decl_source_08.bal", "module-var-decl/module_var_decl_assert_08.json");
    }

    // Recovery tests

    @Test
    public void testModuleVarDeclWithPublicKeyword() {
        testFile("module-var-decl/module_var_decl_source_02.bal", "module-var-decl/module_var_decl_assert_02.json");
    }

    @Test
    public void testConstAndListenersWithoutAssignment() {
        testFile("module-var-decl/module_var_decl_source_03.bal", "module-var-decl/module_var_decl_assert_03.json");
    }

    @Test
    public void testMissingSemicolon() {
        testFile("module-var-decl/module_var_decl_source_04.bal", "module-var-decl/module_var_decl_assert_04.json");
    }

    @Test
    public void testCloseBraceBeforeConst() {
        testFile("module-var-decl/module_var_decl_source_05.bal", "module-var-decl/module_var_decl_assert_05.json");
    }

    @Test
    public void testIncompleteConstDecl() {
        testFile("module-var-decl/module_var_decl_source_06.bal", "module-var-decl/module_var_decl_assert_06.json");
    }

    @Test
    public void testAdditionalTokensInConstAndListener() {
        testFile("module-var-decl/module_var_decl_source_07.bal", "module-var-decl/module_var_decl_assert_07.json");
    }

    @Test
    public void testIncompleteQualifiedIdentifierInListner() {
        testFile("module-var-decl/module_var_decl_source_09.bal", "module-var-decl/module_var_decl_assert_09.json");
    }

    @Test
    public void testIncompleteModuleVarDecl() {
        testFile("module-var-decl/module_var_decl_source_10.bal", "module-var-decl/module_var_decl_assert_10.json");
    }
}
