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
 * Test parsing service declaration.
 * 
 * @since 1.3.0
 */
public class ServiceDeclarationTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testServiceWithoutName() {
        test("service-decl/service_decl_source_01.bal", "service-decl/service_decl_assert_01.json");
    }

    @Test
    public void testServiceWithName() {
        test("service-decl/service_decl_source_02.bal", "service-decl/service_decl_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testMissingServiceKeyword() {
        test("service-decl/service_decl_source_03.bal", "service-decl/service_decl_assert_03.json");
    }

    @Test
    public void testMissingOnKeyword() {
        test("service-decl/service_decl_source_04.bal", "service-decl/service_decl_assert_04.json");
    }

    @Test
    public void testMissingServiceNameAndOnKeyword() {
        test("service-decl/service_decl_source_05.bal", "service-decl/service_decl_assert_05.json");
    }

    @Test
    public void testMissingListener() {
        test("service-decl/service_decl_source_06.bal", "service-decl/service_decl_assert_06.json");
    }

    @Test
    public void testMissingOnKeywordAndListener() {
        test("service-decl/service_decl_source_07.bal", "service-decl/service_decl_assert_07.json");
    }

    @Test
    public void testMissingCommaInListeners() {
        test("service-decl/service_decl_source_08.bal", "service-decl/service_decl_assert_08.json");
    }

    @Test
    public void testMissingOpenAndCloseBraces() {
        test("service-decl/service_decl_source_09.bal", "service-decl/service_decl_assert_09.json");
    }

    @Test
    public void testRecoveryInResources() {
        test("service-decl/service_decl_source_10.bal", "service-decl/service_decl_assert_10.json");
    }

    @Test
    public void testMissingCloseBraceBeforeService1() {
        testFile("service-decl/service_decl_source_11.bal", "service-decl/service_decl_assert_11.json");
    }

    @Test
    public void testMissingCloseBraceBeforeService2() {
        testFile("service-decl/service_decl_source_12.bal", "service-decl/service_decl_assert_12.json");
    }

    @Test
    public void testKeywordsInListenersList() {
        testFile("service-decl/service_decl_source_13.bal", "service-decl/service_decl_assert_13.json");
    }
    
    @Test
    public void testResourceWithoutName() {
        testFile("service-decl/service_decl_source_14.bal", "service-decl/service_decl_assert_14.json");
    }

    @Test
    public void testInvalidResourcesRecovery() {
        test("service-decl/service_decl_source_15.bal", "service-decl/service_decl_assert_15.json");
    }
}
