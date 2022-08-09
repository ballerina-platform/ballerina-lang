/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test parsing client declaration statements.
 * 
 * @since 2201.3.0
 */
public class ClientDeclarationStatementTest extends AbstractStatementTest {

    // Valid syntax tests

    @Test
    public void testClientDeclStmt() {
        testFile("client-decl-stmt/client_decl_stmt_source_01.bal", "client-decl-stmt/client_decl_stmt_assert_01.json");
    }

    @Test
    public void testClientDeclStmtWithAnnotations() {
        testFile("client-decl-stmt/client_decl_stmt_source_02.bal", "client-decl-stmt/client_decl_stmt_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testMissingAliasAndSemicolon() {
        testFile("client-decl-stmt/client_decl_stmt_source_03.bal", "client-decl-stmt/client_decl_stmt_assert_03.json");
    }

    @Test
    public void testMissingAsKeyword() {
        testFile("client-decl-stmt/client_decl_stmt_source_04.bal", "client-decl-stmt/client_decl_stmt_assert_04.json");
    }

    @Test
    public void testMissingClientAndXmlnsKeyword() {
        testFile("client-decl-stmt/client_decl_stmt_source_05.bal", "client-decl-stmt/client_decl_stmt_assert_05.json");
    }

    @Test
    public void testMissingUri() {
        testFile("client-decl-stmt/client_decl_stmt_source_06.bal", "client-decl-stmt/client_decl_stmt_assert_06.json");
    }

    @Test
    public void testIdentifierAsUri() {
        testFile("client-decl-stmt/client_decl_stmt_source_07.bal", "client-decl-stmt/client_decl_stmt_assert_07.json");
    }

    @Test
    public void testMultipleAliases() {
        testFile("client-decl-stmt/client_decl_stmt_source_08.bal", "client-decl-stmt/client_decl_stmt_assert_08.json");
    }

    @Test
    public void testOnlyClientKeyword() {
        testFile("client-decl-stmt/client_decl_stmt_source_09.bal", "client-decl-stmt/client_decl_stmt_assert_09.json");
    }

    @Test
    public void testMissingAlias() {
        testFile("client-decl-stmt/client_decl_stmt_source_10.bal", "client-decl-stmt/client_decl_stmt_assert_10.json");
    }

    @Test
    public void testMissingSemicolon() {
        testFile("client-decl-stmt/client_decl_stmt_source_11.bal", "client-decl-stmt/client_decl_stmt_assert_11.json");
    }

    @Test
    public void testRecoveryWithMultipleClientDeclStmts() {
        testFile("client-decl-stmt/client_decl_stmt_source_12.bal", "client-decl-stmt/client_decl_stmt_assert_12.json");
    }
}
