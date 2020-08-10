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
 * Test parsing optional type.
 */

public class OptionalTypeTest extends AbstractTypesTest {

    //Valid source test

    @Test
    public void testValidOptionalTypeDeclarationWithSimpleType() {
        test("int? a;", "optional-type/optional_type_assert_01.json");
    }

    @Test
    public void testValidOptionalTypeDeclarationWithUserDefinedType() {
        test("T? a;", "optional-type/optional_type_assert_04.json");
    }

    @Test
    public void testValidOptionalTypeDescriptorAsReturnType() {
        testTopLevelNode("optional-type/optional_type_source_01.bal", "optional-type/optional_type_assert_02.json");
    }

    @Test
    public void testLocalValidOptionalTypeDescriptor() {
        testTopLevelNode("optional-type/optional_type_source_02.bal", "optional-type/optional_type_assert_06.json");
    }

    @Test
    public void testValidOptionalTypeDescriptorAsRestParameter() {
        testTopLevelNode("optional-type/optional_type_source_04.bal", "optional-type/optional_type_assert_08.json");
    }

    //Recovery test

    @Test
    public void testInvalidOptionalTypeWithExtraSymbol() {
        test("int? % a;", "optional-type/optional_type_assert_07.json");
    }

    @Test
    public void testInvalidOptionalTypeWithMissingSemicolon() {
        test("int? a", "optional-type/optional_type_assert_03.json");
    }

    @Test
    public void testLocalInValidOptionalTypeDescriptor() {
        testTopLevelNode("optional-type/optional_type_source_03.bal", "optional-type/optional_type_assert_05.json");
    }
}
