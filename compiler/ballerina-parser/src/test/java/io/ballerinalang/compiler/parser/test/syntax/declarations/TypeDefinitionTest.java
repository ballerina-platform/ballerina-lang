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
package io.ballerinalang.compiler.parser.test.syntax.declarations;

import org.testng.annotations.Test;

/**
 * Test parsing type definitions.
 * 
 * @since 2.0.0
 */
public class TypeDefinitionTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testSimpleTypeDefn() {
        testFile("type-def/type_def_source_01.bal", "type-def/type_def_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testIsolatedQualRecovery() {
        testFile("type-def/type_def_source_02.bal", "type-def/type_def_assert_02.json");
    }

    @Test
    public void testObjectNetworkQualRecovery() {
        testFile("type-def/type_def_source_03.bal", "type-def/type_def_assert_03.json");
        testFile("type-def/type_def_source_04.bal", "type-def/type_def_assert_04.json");
    }

    @Test
    public void testTransactionalQualRecovery() {
        testFile("type-def/type_def_source_05.bal", "type-def/type_def_assert_05.json");
    }
}
