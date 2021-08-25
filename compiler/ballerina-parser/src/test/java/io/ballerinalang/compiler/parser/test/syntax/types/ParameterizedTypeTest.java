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
 * Test parsing map and future types.
 */
public class ParameterizedTypeTest extends AbstractTypesTest {

    // Valid source test

    @Test
    public void testValidLocalLevelParameterizedType() {
        testTopLevelNode("parameterized-type/parameterized_type_source_01.bal",
                "parameterized-type/parameterized_type_assert_01.json");
    }

    @Test
    public void testValidModuleLevelMapType() {
        test("map<string> a;", "parameterized-type/parameterized_type_assert_02.json");
    }

    @Test
    public void testSimpleFutureType() {
        test("future a;", "parameterized-type/parameterized_type_assert_15.json");
    }
    
    @Test
    public void testValidModuleLevelFutureTypeWithUserDefinedType() {
        test("future<T> a;", "parameterized-type/parameterized_type_assert_03.json");
    }

    @Test
    public void testValidModuleLevelMapTypeWithOptionalType() {
        test("map<string?> a;", "parameterized-type/parameterized_type_assert_04.json");
    }

    @Test
    public void testValidModuleLevelTypedescTypeWithArrayType() {
        test("typedesc<int[][]> a;", "parameterized-type/parameterized_type_assert_05.json");
    }

    @Test
    public void testValidMapTypeDescAsReturnType() {
        testTopLevelNode("parameterized-type/parameterized_type_source_11.bal",
                "parameterized-type/parameterized_type_assert_11.json");
    }

    // Recovery tests

    @Test
    public void testInValidModuleLevelMapTypeWithMissingGT() {
        test("map<int a;", "parameterized-type/parameterized_type_assert_06.json");
    }

    @Test
    public void testInValidModuleLevelTypedescTypeWithUserDefinedTypeMissingGT() {
        test("typedesc<T a;", "parameterized-type/parameterized_type_assert_07.json");
    }

    @Test
    public void testInValidModuleLevelFutureTypeWithMissingGT() {
        test("future <int a;", "parameterized-type/parameterized_type_assert_08.json");
    }

    @Test
    public void testInValidModuleLevelMapTypeWithMissingLTAndGT() {
        test("map int a;", "parameterized-type/parameterized_type_assert_09.json");
    }

    @Test
    public void testInValidModuleLevelMapTypeWithMissingtype() {
        test("map<> a;", "parameterized-type/parameterized_type_assert_10.json");
    }

    @Test
    public void testInValidModuleLevelMapTypeWithInvalidTokenInside() {
        test("map<%> a;", "parameterized-type/parameterized_type_assert_13.json");
    }

    @Test
    public void testInValidModuleLevelParameterizedTypeMissingType() {
        test("<int> a;", "parameterized-type/parameterized_type_assert_12.json");
    }

    @Test
    public void testMissingParamTypeInMapType() {
        testTopLevelNode("parameterized-type/parameterized_type_source_14.bal",
                "parameterized-type/parameterized_type_assert_14.json");
    }
}
