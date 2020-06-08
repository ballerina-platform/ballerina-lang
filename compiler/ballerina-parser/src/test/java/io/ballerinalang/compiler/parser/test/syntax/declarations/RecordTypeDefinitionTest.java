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
 * Test parsing record type definitions.
 */
public class RecordTypeDefinitionTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testSimpleRecordTypeDef() {
        test("record-type-def/record_type_def_source_01.bal", "record-type-def/record_type_def_assert_01.json");
    }

    @Test
    public void testOptionalAndDefaultValuedFields() {
        test("record-type-def/record_type_def_source_02.bal", "record-type-def/record_type_def_assert_02.json");
    }

    @Test
    public void testRecordTypeReference() {
        test("record-type-def/record_type_def_source_03.bal", "record-type-def/record_type_def_assert_03.json");
    }

    @Test
    public void testClosedRecordRestField() {
        test("record-type-def/record_type_def_source_04.bal", "record-type-def/record_type_def_assert_04.json");
    }

    @Test
    public void testNestedRecordTypeDefs() {
        test("record-type-def/record_type_def_source_11.bal", "record-type-def/record_type_def_assert_11.json");
    }

    @Test
    public void testInlineRecordTypeDefsInFunctions() {
        test("record-type-def/record_type_def_source_13.bal", "record-type-def/record_type_def_assert_13.json");
    }

    @Test
    public void testInlineRecordTypeDefsInFuncParams() {
        test("record-type-def/record_type_def_source_14.bal", "record-type-def/record_type_def_assert_14.json");
    }

    @Test
    public void testInlineRecordTypeDefsInFuncReturn() {
        test("record-type-def/record_type_def_source_15.bal", "record-type-def/record_type_def_assert_15.json");
    }

    @Test
    public void testRecordFieldsWithReadonlyQualifier() {
        test("record-type-def/record_type_def_source_19.bal", "record-type-def/record_type_def_assert_19.json");
    }
    
    // Recovery tests

    @Test
    public void testRecordTypeDescWithMissingSemicolon() {
        test("record-type-def/record_type_def_source_05.bal", "record-type-def/record_type_def_assert_05.json");
    }

    @Test
    public void testRecordFieldsWithMissingSemicolon() {
        test("record-type-def/record_type_def_source_06.bal", "record-type-def/record_type_def_assert_06.json");
    }

    @Test
    public void testMissingTypeReference() {
        test("record-type-def/record_type_def_source_07.bal", "record-type-def/record_type_def_assert_07.json");
    }

    @Test
    public void testRecordTypeDefWithoutName() {
        test("record-type-def/record_type_def_source_08.bal", "record-type-def/record_type_def_assert_08.json");
    }

    @Test
    public void testRecordTypeDefWithoutRecordKeyword() {
        test("record-type-def/record_type_def_source_09.bal", "record-type-def/record_type_def_assert_09.json");
    }

    @Test
    public void testRecordTypeDefWithoutRecordKeywordAndName() {
        testFile("record-type-def/record_type_def_source_10.bal", "record-type-def/record_type_def_assert_10.json");
    }

    @Test
    public void testNestedRecordTypeDefErrorRecovery() {
        test("record-type-def/record_type_def_source_12.bal", "record-type-def/record_type_def_assert_12.json");
    }

    @Test
    public void testNestedRecordTypeDefMissingCloseBrace() {
        test("record-type-def/record_type_def_source_16.bal", "record-type-def/record_type_def_assert_16.json");
    }
}
