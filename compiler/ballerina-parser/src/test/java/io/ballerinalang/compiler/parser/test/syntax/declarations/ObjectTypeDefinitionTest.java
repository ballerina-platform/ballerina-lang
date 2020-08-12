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
 * Test parsing object type definitions.
 */
public class ObjectTypeDefinitionTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testComplexObjectTypeDef() {
        test("object-type-def/object_type_def_source_01.bal", "object-type-def/object_type_def_assert_01.json");
    }

    @Test
    public void testEmptyObjectTypeDef() {
        test("object-type-def/object_type_def_source_02.bal", "object-type-def/object_type_def_assert_02.json");
    }

    @Test
    public void testObjectTypeDefWithFieldsOnly() {
        test("object-type-def/object_type_def_source_03.bal", "object-type-def/object_type_def_assert_03.json");
    }

    @Test
    public void testObjectTypeDefWithMethodsOnly() {
        test("object-type-def/object_type_def_source_04.bal", "object-type-def/object_type_def_assert_04.json");
    }

    @Test
    public void testAnonymousObjectTypeInVarDef() {
        test("object-type-def/object_type_def_source_11.bal", "object-type-def/object_type_def_assert_11.json");
    }

    @Test
    public void testAnonymousObjectTypeInFuncParam() {
        test("object-type-def/object_type_def_source_12.bal", "object-type-def/object_type_def_assert_12.json");
    }

    @Test
    public void testObjectFieldsWithReadonlyTypeDesc() {
        test("object-type-def/object_type_def_source_36.bal", "object-type-def/object_type_def_assert_36.json");
    }

    @Test
    public void testObjectFieldsWithReadonlyQualifier() {
        test("object-type-def/object_type_def_source_37.bal", "object-type-def/object_type_def_assert_37.json");
    }

    @Test
    public void testObjectFieldsWithComplexTypeDescHavingReadonlyTypeDescWithin() {
        test("object-type-def/object_type_def_source_38.bal", "object-type-def/object_type_def_assert_38.json");
        test("object-type-def/object_type_def_source_39.bal", "object-type-def/object_type_def_assert_39.json");
    }

    // Test object type qualifiers

    @Test
    public void testClientQualifierOnly() {
        test("object-type-def/object_type_def_source_14.bal", "object-type-def/object_type_def_assert_14.json");
    }

    @Test
    public void testAbstractQualifierOnly() {
        test("object-type-def/object_type_def_source_15.bal", "object-type-def/object_type_def_assert_15.json");
    }

    @Test
    public void testClientAndAbstractQualifiers() {
        test("object-type-def/object_type_def_source_16.bal", "object-type-def/object_type_def_assert_16.json");
    }

    @Test
    public void testAbstractAndClientQualifiers() {
        test("object-type-def/object_type_def_source_17.bal", "object-type-def/object_type_def_assert_17.json");
    }

    @Test
    public void testReadonlyQualifierOnly() {
        test("object-type-def/object_type_def_source_24.bal", "object-type-def/object_type_def_assert_24.json");
    }

    @Test
    public void testReadonlyAndAbstractQualifiers() {
        test("object-type-def/object_type_def_source_25.bal", "object-type-def/object_type_def_assert_25.json");
    }

    @Test
    public void testAbstractAndReadonlyQualifiers() {
        test("object-type-def/object_type_def_source_26.bal", "object-type-def/object_type_def_assert_26.json");
    }

    @Test
    public void testReadonlyAndClientQualifiers() {
        test("object-type-def/object_type_def_source_27.bal", "object-type-def/object_type_def_assert_27.json");
    }

    @Test
    public void testClientAndReadonlyQualifiers() {
        test("object-type-def/object_type_def_source_28.bal", "object-type-def/object_type_def_assert_28.json");
    }

    @Test
    public void testReadonlyAbstractAndClientQualifiers() {
        test("object-type-def/object_type_def_source_29.bal", "object-type-def/object_type_def_assert_29.json");
    }

    @Test
    public void testReadonlyClientAndAbstractQualifiers() {
        test("object-type-def/object_type_def_source_30.bal", "object-type-def/object_type_def_assert_30.json");
    }

    @Test
    public void testAbstractReadonlyAndClientQualifiers() {
        test("object-type-def/object_type_def_source_31.bal", "object-type-def/object_type_def_assert_31.json");
    }

    @Test
    public void testAbstractClientAndReadonlyQualifiers() {
        test("object-type-def/object_type_def_source_32.bal", "object-type-def/object_type_def_assert_32.json");
    }

    @Test
    public void testClientReadonlyAndAbstractQualifiers() {
        test("object-type-def/object_type_def_source_33.bal", "object-type-def/object_type_def_assert_33.json");
    }

    @Test
    public void testClientAbstractAndReadonlyQualifiers() {
        test("object-type-def/object_type_def_source_34.bal", "object-type-def/object_type_def_assert_34.json");
    }

    @Test
    public void testObjectMethod() {
        test("object-type-def/object_type_def_source_41.bal", "object-type-def/object_type_def_assert_41.json");
    }

    // Recovery tests

    @Test
    public void testMissingCloseBrace() {
        test("object-type-def/object_type_def_source_05.bal", "object-type-def/object_type_def_assert_05.json");
    }

    @Test
    public void testMissingOpenBrace() {
        test("object-type-def/object_type_def_source_06.bal", "object-type-def/object_type_def_assert_06.json");
    }

    @Test
    public void testMissingObjectKeyword() {
        test("object-type-def/object_type_def_source_07.bal", "object-type-def/object_type_def_assert_07.json");
    }

    @Test
    public void testObjectMembersWithExtraTokens() {
        test("object-type-def/object_type_def_source_08.bal", "object-type-def/object_type_def_assert_08.json");
    }

    @Test
    public void testObjectFieldWithMissingEqual() {
        test("object-type-def/object_type_def_source_09.bal", "object-type-def/object_type_def_assert_09.json");
    }

    @Test
    public void testNestedObjectRecovery() {
        test("object-type-def/object_type_def_source_10.bal", "object-type-def/object_type_def_assert_10.json");
    }

    @Test
    public void testMissingTypeReference() {
        test("object-type-def/object_type_def_source_13.bal", "object-type-def/object_type_def_assert_13.json");
    }

    @Test
    public void testDuplicateClientQualifier() {
        test("object-type-def/object_type_def_source_18.bal", "object-type-def/object_type_def_assert_18.json");
    }

    @Test
    public void testDuplicateAbstractQualifier() {
        test("object-type-def/object_type_def_source_19.bal", "object-type-def/object_type_def_assert_19.json");
    }

    @Test
    public void testAdditionalQualifier() {
        test("object-type-def/object_type_def_source_20.bal", "object-type-def/object_type_def_assert_20.json");
    }

    @Test
    public void testAdditionalTokenBetweenQualifiers() {
        test("object-type-def/object_type_def_source_21.bal", "object-type-def/object_type_def_assert_21.json");
    }

    @Test
    public void testDuplicateClientQualifiers() {
        test("object-type-def/object_type_def_source_35.bal", "object-type-def/object_type_def_assert_35.json");
    }

    @Test
    public void testInvalidTokenInObjectmembers() {
        test("object-type-def/object_type_def_source_40.bal", "object-type-def/object_type_def_assert_40.json");
    }

    @Test
    public void testInvalidObjectMethodWithTransactional() {
        test("object-type-def/object_type_def_source_42.bal", "object-type-def/object_type_def_assert_42.json");
    }
}
