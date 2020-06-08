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

package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for selectively immutable values with the `readonly` type.
 *
 * @since 1.3.0
 */
public class SelectivelyImmutableTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/readonly/test_selectively_immutable_type.bal");
    }

    @Test
    public void testReadonlyType() {
        BRunUtil.invoke(result, "testReadonlyType");
    }

    @Test
    public void testReadonlyRecordFieldsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/types/readonly/test_selectively_immutable_type_negative.bal");
        int index = 0;

        // Assignment and initialization.
        validateError(result, index++, "incompatible types: expected 'Student', found '(Person & readonly)'", 41, 17);
        validateError(result, index++, "incompatible types: expected '(int|string)', found '(PersonalDetails & " +
                "readonly)'", 42, 20);
        validateError(result, index++, "incompatible types: expected 'Student', found '(Person & readonly)?'", 43, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'", 44, 16);
        validateError(result, index++, "incompatible types: expected '(Student & readonly)', found 'Student'", 57, 30);
        validateError(result, index++, "incompatible types: expected '(PersonalDetails & readonly)', found " +
                "'PersonalDetails'", 60, 18);
        validateError(result, index++, "incompatible types: expected '((A|B|any) & readonly)', found 'Obj'", 78, 26);
        validateError(result, index++, "incompatible types: expected '(PersonalDetails & readonly)', found " +
                "'PersonalDetails'", 105, 18);
        validateError(result, index++, "incompatible types: expected '(Department & readonly)' for field 'dept', " +
                "found 'Department'", 106, 12);

        // Updates.
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 129, 5);
        validateError(result, index++, "a type compatible with mapping constructor expressions not found in type " +
                "'other'", 129, 17);
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 133, 5);
        validateError(result, index++, "incompatible types: expected 'Department & readonly', found 'Department'", 138,
                      14);
        validateError(result, index++, "incompatible types: expected 'Department & readonly', found 'Department'", 139,
                      17);
        validateError(result, index++, "invalid intersection type with 'readonly', 'table<Bar> key(name)' can never " +
                              "be 'readonly'", 152, 5);

        assertEquals(result.getErrorCount(), index);
    }
}
