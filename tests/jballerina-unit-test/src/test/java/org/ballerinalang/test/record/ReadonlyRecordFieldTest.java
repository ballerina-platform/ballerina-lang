/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.record;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `readonly` record fields.
 *
 * @since 1.3.0
 */
public class ReadonlyRecordFieldTest {

    @Test(dataProvider = "readonlyRecordFieldTestFunctions")
    public void testReadonlyRecordFields(String testFunction) {
        CompileResult result = BCompileUtil.compile("test-src/record/readonly_record_fields.bal");
        BRunUtil.invoke(result, testFunction);
    }

    @DataProvider(name = "readonlyRecordFieldTestFunctions")
    public Object[][] readonlyRecordFieldTestFunctions() {
        return new Object[][]{
                {"testRecordWithSimpleReadonlyFields"},
                {"testInvalidRecordSimpleReadonlyFieldUpdate"},
                {"testValidUpdateOfPossiblyReadonlyFieldInUnion"},
                {"testInvalidUpdateOfPossiblyReadonlyFieldInUnion"},
                {"testRecordWithStructuredReadonlyFields"},
                {"testReadOnlyFieldWithDefaultValue"},
                {"testTypeReadOnlyFlagForAllReadOnlyFields"},
                {"testTypeReadOnlyFlagForAllReadOnlyFieldsInAnonymousRecord"},
                {"testSubTypingWithReadOnlyFields"},
                {"testSubTypingWithReadOnlyFieldsViaReadOnlyType"},
                {"testSubTypingWithReadOnlyFieldsNegative"},
                {"testSubTypingWithReadOnlyFieldsPositiveComposite"},
                {"testSubTypingWithReadOnlyFieldsNegativeComposite"},
                {"testSubTypingMapAsRecordWithReadOnlyFields"},
                {"testReadOnlyFieldsOfClassTypes"},
                {"testTypeReadOnlynessNegativeWithNonReadOnlyFieldsViaInclusion"},
                {"testTypeReadOnlynessWithReadOnlyFieldsViaInclusion"},
                {"testRecordWithFunctionTypeField"},
                {"testDefaultValueFromCETBeingUsedWithReadOnlyFieldsInTheMappingConstructor"}
        };
    }

    @Test
    public void testReadonlyRecordFieldsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/record/readonly_record_fields_negative.bal");
        int index = 0;

        validateError(result, index++, "cannot update 'readonly' record field 'name' in 'Student'", 27, 5);
        validateError(result, index++, "cannot update 'readonly' record field 'name' in 'Student'", 28, 5);
        validateError(result, index++, "incompatible types: expected '(Details & readonly)', found 'Details'", 52, 9);
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 56, 5);
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 57, 5);
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 58, 5);
        validateError(result, index++, "cannot update 'readonly' record field 'name' in '(Student|Customer)'", 77, 5);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Bar'", 106, 25);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Baz'", 107, 25);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Qux'", 108, 25);
        validateError(result, index++, "incompatible types: expected 'Person', found 'Undergraduate'", 142, 17);
        validateError(result, index++, "incompatible types: expected 'Person', found 'Graduate'", 150, 17);
        validateError(result, index++, "incompatible types: expected 'OptionalId', found 'map<(map<int>|boolean)>'",
                      159, 23);
        validateError(result, index++, "cannot update 'readonly' record field 'code' in 'Quux'", 169, 5);
        validateError(result, index++, "incompatible types: expected 'record {| readonly int i; string s; readonly " +
                "boolean b; |}', found 'record {| int i; string s; boolean b; |}'", 183, 12);
        validateError(result, index++, "invalid 'readonly' field, 'stream<int>' can never be 'readonly'", 191, 9);
        validateError(result, index++, "cannot initialize abstract object '(NonReadOnlyClass & readonly)'", 192, 17);
        validateError(result, index++, "cannot initialize abstract object '(ReadOnlyClass & readonly)'", 205, 43);
        validateError(result, index++, "invalid 'readonly' field, 'stream<int>' can never be 'readonly'", 206, 5);
        validateError(result, index++, "cannot initialize abstract object '(NonReadOnlyClass & readonly)'", 209, 69);
        validateError(result, index++, "cannot initialize abstract object '(ReadOnlyClass & readonly)'", 209, 77);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'record {| readonly int x; " +
                "anydata...; |}'", 227, 15);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'record {| readonly int x; " +
                "anydata...; |}'", 242, 19);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenRecordWithNoFieldDescriptors'", 243, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenRecordWithNoFieldDescriptors'", 244, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenIntRecordWithNoFieldDescriptors'", 245, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenIntRecordWithNoFieldDescriptors'", 246, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenRecordWithFieldDescriptors'", 247, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found " +
                "'OpenRecordWithFieldDescriptors'", 248, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'record {| readonly int x; int" +
                "...; |}'", 249, 20);
        validateError(result, index++, "incompatible types: expected 'readonly', found '(Unauthorized|Forbidden)?'",
                      269, 19);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'Unauthorized?'", 272, 19);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'Unauthorized?'", 273, 18);
        validateError(result, index++, "missing non-defaultable required record field 'y'", 285, 42);
        validateError(result, index++, "incompatible types: expected 'RecordWithReadOnlyFields', found 'int'", 286, 53);
        assertEquals(result.getErrorCount(), index);
    }
}
