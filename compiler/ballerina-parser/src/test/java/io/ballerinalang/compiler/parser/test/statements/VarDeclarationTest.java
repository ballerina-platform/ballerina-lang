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

    @Test
    public void testLocalVarDeclWithBuiltinType() {
        test("int a = 5;", "var-decl-stmt/local_var_decl_assert_1.json");
    }

    @Test
    public void testLocalVarDeclWithUserdefinedType() {
        test("Foo a = 5;", "var-decl-stmt/local_var_decl_assert_2.json");
    }

    @Test
    public void testLocalVarDeclWithMissingSemicolon() {
        test("Foo a = 5", "var-decl-stmt/local_var_decl_assert_3.json");
    }

    @Test
    public void testLocalVarDeclWithMissingEqual() {
        test("Foo a 5;", "var-decl-stmt/local_var_decl_assert_4.json");
    }
}
