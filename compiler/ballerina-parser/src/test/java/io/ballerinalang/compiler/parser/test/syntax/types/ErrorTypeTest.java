/*
 * Copyright (c) 2020, WSO2 InValidc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 InValidc. licenses this file to you under the Apache License,
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
 * Test parsing error type.
 */
public class ErrorTypeTest extends AbstractTypesTest {

    //Valid source tests
    @Test
    public void testValidLocalLevelErrorType() {
        testTopLevelNode("error-type/error_type_assert_01.bal", "error-type/error_type_assert_01.json");
    }

    @Test
    public void testValidModuleLevelErrorType() {
        test("error<NO_MATCHING_OBJECT> a;", "error-type/error_type_assert_02.json");
    }

    @Test
    public void testValidModuleLevelErrorTypeWithAsterisk() {
        test("error<*> a;", "error-type/error_type_assert_03.json");
    }

    @Test
    public void testValidMErrorTypeAsReturnType() {
        testTopLevelNode("error-type/error_type_assert_02.bal", "error-type/error_type_assert_04.json");
    }

    @Test
    public void testValidMErrorTypeAsTypeDefinition() {
        testTopLevelNode("error-type/error_type_assert_03.bal", "error-type/error_type_assert_05.json");
    }

    //Recovery tests
    @Test
    public void testInValidErrorTypeMissingErrorKeyword() {
        test("<NO_MATCHING_OBJECT> a;", "error-type/error_type_assert_06.json");
    }

    @Test
    public void testInValidErrorTypeMissingLtToken() {
        test("error NO_MATCHING_OBJECT> a;", "error-type/error_type_assert_07.json");
    }

    @Test
    public void testInValidErrorTypeMissingGtToken() {
        test("error <NO_MATCHING_OBJECT a;", "error-type/error_type_assert_08.json");
    }

    @Test
    public void testInValidErrorTypeMissingErrorTypeParams() {
        test("error<> a;", "error-type/error_type_assert_09.json");
    }

    @Test
    public void testInValidErrorTypeExtraGt() {
        test("error<NO_MATCHING_OBJECT>> a;", "error-type/error_type_assert_10.json");
    }

    @Test
    public void testInValidErrorTypeExtraSymbol() {
        test("error<NO_MATCHING_OBJECT%> a;", "error-type/error_type_assert_02.json");
    }
}
