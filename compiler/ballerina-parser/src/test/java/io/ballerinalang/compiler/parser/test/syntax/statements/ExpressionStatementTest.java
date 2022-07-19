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

    // Test expression statement

    @Test
    public void testExpressionStmtForSimpleLocalVarReference() {
        testFile("var-decl-stmt/local_var_decl_source_18.bal", "var-decl-stmt/local_var_decl_assert_18.json");
        testFile("var-decl-stmt/local_var_decl_source_20.bal", "var-decl-stmt/local_var_decl_assert_20.json");
    }
    @Test
    public void testExpressionStmtForSimpleModuleVarReference() {
        testFile("var-decl-stmt/module_var_decl_source_20.bal", "var-decl-stmt/module_var_decl_assert_20.json");
        testFile("var-decl-stmt/module_var_decl_source_22.bal", "var-decl-stmt/module_var_decl_assert_22.json");
    }

    // Test invalid expression statement

    @Test
    public void testExpressionStmtForByteArrayLiteral() {
        testFile("expression-stmt/expression_stmt_source_01.bal", "expression-stmt/expression_stmt_assert_01.json");
    }

    @Test
    public void testExpressionStmtForListConstructor() {
        testFile("expression-stmt/expression_stmt_source_02.bal", "expression-stmt/expression_stmt_assert_02.json");
    }

    // Test expression statement with recovery

    @Test
    public void testExpressionStmtForSimpleLocalVarReferenceWithRecovery() {
        testFile("var-decl-stmt/local_var_decl_source_17.bal", "var-decl-stmt/local_var_decl_assert_17.json");
        testFile("var-decl-stmt/local_var_decl_source_19.bal", "var-decl-stmt/local_var_decl_assert_19.json");
    }

    @Test
    public void testExpressionStmtForSimpleModuleVarReferenceWithRecovery() {
        testFile("var-decl-stmt/module_var_decl_source_19.bal", "var-decl-stmt/module_var_decl_assert_19.json");
        testFile("var-decl-stmt/module_var_decl_source_21.bal", "var-decl-stmt/module_var_decl_assert_21.json");
    }
}
