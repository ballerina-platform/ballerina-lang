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
 * Test parsing table type.
 */
public class TableTypeTest extends AbstractTypesTest {

    // Valid source tests

    @Test
    public void testValidLocalLevelTableTypes() {
        testTopLevelNode("table-type/table_type_source_01.bal", "table-type/table_type_assert_01.json");
    }

    @Test
    public void testValidModuleLevelTableType() {
        test("table<T> a;", "table-type/table_type_assert_02.json");
        test("table<Myrecord> key(id,name) a;", "table-type/table_type_assert_03.json");
        test("table<Myrecord> key<int> a;", "table-type/table_type_assert_04.json");
        test("table<Myrecord> key() a;", "table-type/table_type_assert_05.json");
    }

    @Test
    public void testValidTableTypeAsReturnType() {
        testTopLevelNode("table-type/table_type_source_06.bal", "table-type/table_type_assert_06.json");
    }

    @Test
    public void testValidTableTypeAsTypeDefinition() {
        testTopLevelNode("table-type/table_type_source_07.bal", "table-type/table_type_assert_07.json");
    }

    // Recovery tests

    @Test
    public void testInValidLocalLevelTableTypes() {
        testTopLevelNode("table-type/table_type_source_08.bal", "table-type/table_type_assert_08.json");
    }
}
