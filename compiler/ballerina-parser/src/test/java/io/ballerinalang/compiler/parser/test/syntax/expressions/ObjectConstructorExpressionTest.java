/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing <code>object-constructor-expr</code>.
 */
public class ObjectConstructorExpressionTest extends AbstractExpressionsTest {

    // valid syntax

    @Test
    public void testSmallestObjectConstructor() {
        test("object {}", "object-constructor/object-constructor-smallest.json");
    }

    @Test
    public void testObjectConstructorWithAnnotation() {
        testFile("object-constructor/object-constructor-with-annotations.bal",
                "object-constructor/object-constructor-with-annotations.json");
    }

    @Test
    public void testObjectConstructorWithBasicObjectFields() {
        testFile("object-constructor/object-constructor-with-basic-object-fields.bal",
                "object-constructor/object-constructor-with-basic-object-fields.json");
    }

    @Test
    public void testObjectConstructorWithClientKeyword() {
        testFile("object-constructor/object-constructor-with-client-keyword.bal",
                "object-constructor/object-constructor-with-client-keyword.json");
    }

    @Test
    public void testObjectConstructorWithMethods() {
        testFile("object-constructor/object-constructor-with-methods.bal",
                "object-constructor/object-constructor-with-methods.json");
    }

    @Test
    public void testObjectConstructorWithTypeReference() {
        testFile("object-constructor/object-constructor-with-type-reference.bal",
                "object-constructor/object-constructor-with-type-reference.json");
    }

    @Test
    public void testObjectFieldsWithFinalQualifier() {
        testFile("object-constructor/object_constructor_source_08.bal",
                "object-constructor/object_constructor_assert_08.json");
    }

    // Recovery tests

    @Test
    public void testObjectConstructorWithFieldsNegative() {
        testFile("object-constructor/object_constructor_source_06.bal",
                "object-constructor/object_constructor_assert_06.json");
        testFile("object-constructor/object_constructor_source_07.bal",
                "object-constructor/object_constructor_assert_07.json");
    }

    @Test
    public void testObjectConstructorForMissingOpenCloseBraces() {
        testFile("object-constructor/object_constructor_source_01.bal",
                "object-constructor/object_constructor_assert_01.json");
        testFile("object-constructor/object_constructor_source_02.bal",
                "object-constructor/object_constructor_assert_02.json");
    }

    @Test
    public void testObjectConstructorForObjectKeywordRecovery() {
        testFile("object-constructor/object_constructor_source_03.bal",
                "object-constructor/object_constructor_assert_03.json");
        testFile("object-constructor/object_constructor_source_04.bal",
                "object-constructor/object_constructor_assert_04.json");
    }

    @Test
    public void testObjectConstructorTypeReferenceRecovery() {
        testFile("object-constructor/object_constructor_source_05.bal",
                "object-constructor/object_constructor_assert_05.json");
    }

    @Test
    public void testMethodDefinitionWithFinalQualifier() {
        testFile("object-constructor/object_constructor_source_09.bal",
                "object-constructor/object_constructor_assert_09.json");
    }
}
