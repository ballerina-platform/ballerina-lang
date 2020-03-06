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
package io.ballerinalang.compiler.parser.test.statements;

import org.testng.annotations.Test;

/**
 * Test parsing variable declaration statements.
 */
public class VarDeclarationTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testLocalVarDeclWithBuiltinType() {
        test("int a;", "var-decl-stmt/local_var_decl_assert_1.json");
    }

    @Test
    public void testLocalVarDeclWithUserdefinedType() {
        test("Foo a;", "var-decl-stmt/local_var_decl_assert_2.json");
    }

    @Test
    public void testLocalVarDeclWithBuiltinTypeAndRhs() {
        test("int a = 5;", "var-decl-stmt/local_var_decl_assert_3.json");
    }

    @Test
    public void testLocalVarDeclWithUserdefinedTypeAndRhs() {
        test("Foo a = 5;", "var-decl-stmt/local_var_decl_assert_4.json");
    }

    // Recovery tests

    @Test
    public void testLocalVarDeclWithMissingSemicolon() {
        test("Foo a", "var-decl-stmt/local_var_decl_assert_5.json");
    }

    @Test
    public void testLocalVarDeclWithRhsAndMissingSemicolon() {
        test("Foo a = 5", "var-decl-stmt/local_var_decl_assert_6.json");
    }

    @Test
    public void testLocalVarDeclWithRhsAndMissingEqual() {
        test("Foo a 5;", "var-decl-stmt/local_var_decl_assert_7.json");
    }

    @Test
    public void testLocalVarDeclWithBuiltinTypeAndMissingIdentifier() {
        test("int ;", "var-decl-stmt/local_var_decl_assert_8.json");
    }

    @Test
    public void testLocalVarDeclWithUserdefinedTypeAndMissingIdentifier() {
        test("Foo ;", "var-decl-stmt/local_var_decl_assert_9.json");
    }
}
