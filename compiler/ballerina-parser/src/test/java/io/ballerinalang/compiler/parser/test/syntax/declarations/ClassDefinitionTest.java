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
public class ClassDefinitionTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testComplexObjectTypeDef() {
        test("class-def/class_def_source_01.bal", "class-def/class_def_assert_01.json");
    }

    @Test
    public void testEmptyObjectTypeDef() {
        test("class-def/class_def_source_02.bal", "class-def/class_def_assert_02.json");
    }

    @Test
    public void testObjectTypeDefWithFieldsOnly() {
        test("class-def/class_def_source_03.bal", "class-def/class_def_assert_03.json");
    }

    @Test
    public void testObjectTypeDefWithMethodsOnly() {
        test("class-def/class_def_source_04.bal", "class-def/class_def_assert_04.json");
    }

    @Test
    public void testObjectFieldsWithReadonlyTypeDesc() {
        test("class-def/class_def_source_36.bal", "class-def/class_def_assert_36.json");
    }

    @Test
    public void testObjectFieldsWithReadonlyQualifier() {
        test("class-def/class_def_source_37.bal", "class-def/class_def_assert_37.json");
    }

    @Test
    public void testObjectFieldsWithComplexTypeDescHavingReadonlyTypeDescWithin() {
        test("class-def/class_def_source_38.bal", "class-def/class_def_assert_38.json");
        test("class-def/class_def_source_39.bal", "class-def/class_def_assert_39.json");
    }

    // Test object type qualifiers

    // todo: improve parser error handling for class defn
    // error recovery doesn't support class type qualifiers after class keyword.
    // class client Foo
    // @Test
    public void testClientQualifierOnly() {
        test("class-def/class_def_source_14.bal", "class-def/class_def_assert_14.json");
    }

    @Test
    public void testReadonlyQualifierOnly() {
        test("class-def/class_def_source_24.bal", "class-def/class_def_assert_24.json");
    }

    @Test
    public void testReadonlyAndAbstractQualifiers() {
        test("class-def/class_def_source_25.bal", "class-def/class_def_assert_25.json");
    }

    @Test
    public void testReadonlyAndClientQualifiers() {
        test("class-def/class_def_source_27.bal", "class-def/class_def_assert_27.json");
    }

    @Test
    public void testClientAndReadonlyQualifiers() {
        test("class-def/class_def_source_28.bal", "class-def/class_def_assert_28.json");
    }

    // Recovery tests

    @Test
    public void testMissingCloseBrace() {
        test("class-def/class_def_source_05.bal", "class-def/class_def_assert_05.json");
    }

    @Test
    public void testMissingOpenBrace() {
        test("class-def/class_def_source_06.bal", "class-def/class_def_assert_06.json");
    }

    @Test
    public void testMissingObjectKeyword() {
        test("class-def/class_def_source_07.bal", "class-def/class_def_assert_07.json");
    }

    @Test
    public void testObjectMembersWithExtraTokens() {
        test("class-def/class_def_source_08.bal", "class-def/class_def_assert_08.json");
    }

    @Test
    public void testObjectFieldWithMissingEqual() {
        test("class-def/class_def_source_09.bal", "class-def/class_def_assert_09.json");
    }

    @Test
    public void testNestedObjectRecovery() {
        test("class-def/class_def_source_10.bal", "class-def/class_def_assert_10.json");
    }

    @Test
    public void testMissingTypeReference() {
        test("class-def/class_def_source_13.bal", "class-def/class_def_assert_13.json");
    }

    @Test
    public void testDuplicateClientQualifier() {
        test("class-def/class_def_source_18.bal", "class-def/class_def_assert_18.json");
    }

    @Test
    public void testDuplicateClientQualifiers() {
        test("class-def/class_def_source_35.bal", "class-def/class_def_assert_35.json");
    }
}
