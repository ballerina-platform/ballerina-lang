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
package io.ballerinalang.compiler.parser.test.syntax.declarations;

import org.testng.annotations.Test;

/**
 * Test parsing client declarations.
 * 
 * @since 2201.3.0
 */
public class ClientDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testClientDecl() {
        testFile("client-decl/client_decl_source_01.bal", "client-decl/client_decl_assert_01.json");
    }

    @Test
    public void testClientDeclWithAnnotations() {
        testFile("client-decl/client_decl_source_02.bal", "client-decl/client_decl_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testMissingAliasAndSemicolon() {
        testFile("client-decl/client_decl_source_03.bal", "client-decl/client_decl_assert_03.json");
    }

    @Test
    public void testMissingAsKeyword() {
        testFile("client-decl/client_decl_source_04.bal", "client-decl/client_decl_assert_04.json");
    }

    @Test
    public void testMissingClientAndXmlnsKeyword() {
        testFile("client-decl/client_decl_source_05.bal", "client-decl/client_decl_assert_05.json");
    }

    @Test
    public void testMissingUri() {
        testFile("client-decl/client_decl_source_06.bal", "client-decl/client_decl_assert_06.json");
    }

    @Test
    public void testIdentifierAsUri() {
        testFile("client-decl/client_decl_source_07.bal", "client-decl/client_decl_assert_07.json");
    }

    @Test
    public void testMultipleAliases() {
        testFile("client-decl/client_decl_source_08.bal", "client-decl/client_decl_assert_08.json");
    }

    @Test
    public void testOnlyClientKeyword() {
        testFile("client-decl/client_decl_source_09.bal", "client-decl/client_decl_assert_09.json");
    }

    @Test
    public void testMissingAlias() {
        testFile("client-decl/client_decl_source_10.bal", "client-decl/client_decl_assert_10.json");
    }

    @Test
    public void testMissingSemicolon() {
        testFile("client-decl/client_decl_source_11.bal", "client-decl/client_decl_assert_11.json");
    }

    @Test
    public void testRecoveryWithMultipleClientDecls() {
        testFile("client-decl/client_decl_source_12.bal", "client-decl/client_decl_assert_12.json");
    }

    @Test
    public void testInvalidQualifiers() {
        testFile("client-decl/client_decl_source_13.bal", "client-decl/client_decl_assert_13.json");
    }
}
