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
 * Test parsing union type.
 */
public class UnionTypeTest extends AbstractTypesTest {

    // Valid source test
    
    @Test
    public void testValidLocalLevelUnionType() {
        testTopLevelNode("union-type/union_type_source_01.bal", "union-type/union_type_assert_01.json");
    }

    @Test
    public void testValidModuleLevelUnionType() {
        test("int|string a;", "union-type/union_type_assert_04.json");
    }

    @Test
    public void testValidModuleLevelUnionTypeWithUserDefinedType() {
        test("T|A a;", "union-type/union_type_assert_05.json");
    }

    // Recovery test

    @Test
    public void testInValidLocalLevelUnionTypeMissingRightTypeDesc() {
        testFile("union-type/union_type_source_02.bal", "union-type/union_type_assert_02.json");
    }

    @Test
    public void testInValidLocalLevelUnionTypeExtraSymbols() {
        testTopLevelNode("union-type/union_type_source_03.bal", "union-type/union_type_assert_03.json");
    }

    @Test
    public void testInValidLocalLevelUnionTypeMissingLeftTypeDesc() {
        testTopLevelNode("union-type/union_type_source_06.bal", "union-type/union_type_assert_06.json");
    }

    @Test
    public void testUnionTypeDescInvalidSyntax() {
        testTopLevelNode("union-type/union_type_source_07.bal", "union-type/union_type_assert_07.json");
    }
}
