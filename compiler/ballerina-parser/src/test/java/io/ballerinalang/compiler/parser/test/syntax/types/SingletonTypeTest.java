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
 * Test parsing singleton type.
 */
public class SingletonTypeTest extends AbstractTypesTest {

    // Valid source test

    @Test
    public void testValidLocalLevelSingletonType() {
        testTopLevelNode("singleton-type/singleton_type_source_01.bal",
                "singleton-type/singleton_type_assert_01.json");
    }

    @Test
    public void testValidSingletonTypeAsReturnType() {
        testTopLevelNode("singleton-type/singleton_type_source_02.bal",
                "singleton-type/singleton_type_assert_02.json");
    }

    @Test
    public void testValidSingletonTypeAsTypeDefinition() {
        testTopLevelNode("singleton-type/singleton_type_source_03.bal",
                "singleton-type/singleton_type_assert_03.json");
    }

    @Test
    public void testValidModuleLevelSingletonType() {
        test("true a;", "singleton-type/singleton_type_assert_04.json");
    }

    // Recovery tests

    @Test
    public void testInValidSingletonTypeAsReturnType() {
        testTopLevelNode("singleton-type/singleton_type_source_05.bal",
                "singleton-type/singleton_type_assert_05.json");
    }

    @Test
    public void testInValidSingletonTypeAsTypeDefinitionMissingInt() {
        testTopLevelNode("singleton-type/singleton_type_source_06.bal",
                "singleton-type/singleton_type_assert_06.json");
    }

    @Test
    public void testInvalidModuleLevelSingletonType() {
        test("+ % a;",
                "singleton-type/singleton_type_assert_07.json");
    }

    @Test
    public void testInValidLocalLevelSingletonTypes() {
        testTopLevelNode("singleton-type/singleton_type_source_08.bal",
                "singleton-type/singleton_type_assert_08.json");
    }
}
