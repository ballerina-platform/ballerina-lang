/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.syntax.statements;

import org.testng.annotations.Test;

/**
 * Test parsing expression statements.
 *
 * @since 2.0.0
 */
public class ExpressionStatementTest extends AbstractStatementTest {

    // Test invalid expression statement without recovery

    @Test
    public void testExpressionStmtForSimpleLocalVarReference() {
        testFile("expression-stmt/local_var_decl_source_2.bal", "expression-stmt/local_var_decl_assert_2.json");
        testFile("expression-stmt/local_var_decl_source_4.bal", "expression-stmt/local_var_decl_assert_4.json");
    }
    
    @Test
    public void testExpressionStmtForSimpleModuleVarReference() {
        testFile("expression-stmt/module_var_decl_source_2.bal", "expression-stmt/module_var_decl_assert_2.json");
        testFile("expression-stmt/module_var_decl_source_4.bal", "expression-stmt/module_var_decl_assert_4.json");
    }

    @Test
    public void testExpressionStmtForByteArrayLiteral() {
        testFile("expression-stmt/expression_stmt_source_01.bal", "expression-stmt/expression_stmt_assert_01.json");
    }

    @Test
    public void testExpressionStmtForListConstructor() {
        testFile("expression-stmt/expression_stmt_source_02.bal", "expression-stmt/expression_stmt_assert_02.json");
    }

    @Test
    public void testExpressionStmtForQueryConstructsMap() {
        test("map from int i in y let int a = i * 2 where a < 10 select a on conflict error(\"Error\");\n",
                "expression-stmt/expression_stmt_assert_03.json");
    }

    // Test expression statement with recovery

    @Test
    public void testExpressionStmtForSimpleLocalVarReferenceWithRecovery() {
        testFile("expression-stmt/local_var_decl_source_1.bal", "expression-stmt/local_var_decl_assert_1.json");
        testFile("expression-stmt/local_var_decl_source_3.bal", "expression-stmt/local_var_decl_assert_3.json");
        testFile("expression-stmt/local_var_decl_source_5.bal", "expression-stmt/local_var_decl_assert_5.json");
        testFile("expression-stmt/local_var_decl_source_6.bal", "expression-stmt/local_var_decl_assert_6.json");
    }

    @Test
    public void testExpressionStmtForSimpleModuleVarReferenceWithRecovery() {
        testFile("expression-stmt/module_var_decl_source_1.bal", "expression-stmt/module_var_decl_assert_1.json");
        testFile("expression-stmt/module_var_decl_source_3.bal", "expression-stmt/module_var_decl_assert_3.json");
        testFile("expression-stmt/module_var_decl_source_5.bal", "expression-stmt/module_var_decl_assert_5.json");
        testFile("expression-stmt/module_var_decl_source_6.bal", "expression-stmt/module_var_decl_assert_6.json");
    }
}
