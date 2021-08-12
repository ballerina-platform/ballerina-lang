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
 * Test parsing stream type.
 */
public class StreamTypeTest extends AbstractTypesTest {

    // Valid source tests

    @Test
    public void testValidLocalLevelStreamTypes() {
        testTopLevelNode("stream-type/stream_type_source_01.bal", "stream-type/stream_type_assert_01.json");
    }

    @Test
    public void testValidModuleLevelStreamType() {
        test("stream<T, error> a;", "stream-type/stream_type_assert_02.json");
    }

    @Test
    public void testValidStreamTypeAsReturnType() {
        testTopLevelNode("stream-type/stream_type_source_03.bal", "stream-type/stream_type_assert_03.json");
    }

    @Test
    public void testValidStreamTypeAsTypeDefinition() {
        testTopLevelNode("stream-type/stream_type_source_04.bal", "stream-type/stream_type_assert_04.json");
    }

    // Recovery tests

    @Test
    public void testInValidLocalLevelStreamTypes() {
        testTopLevelNode("stream-type/stream_type_source_05.bal", "stream-type/stream_type_assert_05.json");
    }

    @Test
    public void testInValidModuleLevelStreamTypeMissingComma() {
        test("stream<int error> a;", "stream-type/stream_type_assert_06.json");
    }

    @Test
    public void testInValidModuleLevelStreamTypeMissingGT() {
        test("stream<string a;", "stream-type/stream_type_assert_07.json");
    }

    @Test
    public void testInValidModuleLevelStreamTypeMissingTypeDesc() {
        test("stream<> a;", "stream-type/stream_type_assert_08.json");
    }

    @Test
    public void testInValidModuleLevelStreamTypeExtraGt() {
        test("stream<string>> a;", "stream-type/stream_type_assert_09.json");
    }
}
