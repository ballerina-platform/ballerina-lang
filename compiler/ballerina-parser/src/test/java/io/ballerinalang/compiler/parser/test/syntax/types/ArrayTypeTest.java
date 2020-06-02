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
package io.ballerinalang.compiler.parser.test.syntax.types;

import org.testng.annotations.Test;

/**
 * Test parsing array type.
 */

public class ArrayTypeTest extends AbstractTypesTest {

    //Valid source test

    @Test
    public void testValidArrayTypeWithSimpleType() {
        test("int[] a;", "array-type/array_type_assert_01.json");
    }

    @Test
    public void testValidArrayTypeDeclarationWithUserDefinedType() {
        test("T[] a;", "array-type/array_type_assert_03.json");
    }

    @Test
    public void testValidArrayTypeDescriptorAsReturnType() {
        testTopLevelNode("array-type/array_type_assert_01.bal", "array-type/array_type_assert_02.json");
    }

    @Test
    public void testValidArrayTypeDescriptorDeclarationWithLength() {
        test("int[5] a;", "array-type/array_type_assert_04.json");
    }

    @Test
    public void testValidArrayTypeDescriptorDeclarationWithOptionalType() {
        test("int?[5] a;", "array-type/array_type_assert_05.json");
    }

    @Test
    public void testValidTwoDimensionalArrayTypeWithSimpleType() {
        test("int[][] a;", "array-type/array_type_assert_06.json");
    }

    @Test
    public void testValidTwoDimensionalArrayTypeDeclarationWithUserDefinedType() {
        test("T[][] a;", "array-type/array_type_assert_07.json");
    }

    @Test
    public void testValidTwoDimensionalArrayTypeDescriptorAsReturnType() {
        testTopLevelNode("array-type/array_type_assert_02.bal", "array-type/array_type_assert_08.json");
    }

    @Test
    public void testValidTwoDimensionalArrayTypeDescriptorDeclarationWithLength() {
        test("int[5][8] a;", "array-type/array_type_assert_09.json");
    }

    @Test
    public void testValidThreeDimensionalArrayTypeWithSimpleType() {
        test("int[][][] a;", "array-type/array_type_assert_13.json");
    }

    @Test
    public void testValidOptionalTypeDescContainsArrayTypeDesc() {
        test("int?[]? a;", "array-type/array_type_assert_14.json");
    }

    @Test
    public void testValidArrayTypeDescWithQualifiedIdentifier() {
        test("int[b:c] a;", "array-type/array_type_assert_15.json");
    }

    @Test
    public void testLocalValidArrayType() {
        testTopLevelNode("array-type/array_type_assert_03.bal", "array-type/array_type_assert_16.json");
    }

    //Recovery test

    @Test
    public void testInvalidArrayTypeMissingCloseBracket() {
        test("int[ a;", "array-type/array_type_assert_10.json");
    }

    @Test
    public void testInvalidArrayTypeWithArrayLengthMissingCloseBracket() {
        test("int[5 a;", "array-type/array_type_assert_11.json");
    }

    @Test
    public void testInvalidArrayTypeExtraCloseBracket() {
        test("int[]] a;", "array-type/array_type_assert_01.json");
    }

    @Test
    public void testInvalidArrayTypeInvalidArrayLength() {
        test("int[&%] a;", "array-type/array_type_assert_01.json");
    }

    @Test
    public void testInvalidTwoDimensionalArrayTypeMissingCloseBracket() {
        test("T[][ a;", "array-type/array_type_assert_12.json");
    }

    @Test
    public void testLocalInValidArrayType() {
        testTopLevelNode("array-type/array_type_assert_04.bal", "array-type/array_type_assert_17.json");
    }

}
