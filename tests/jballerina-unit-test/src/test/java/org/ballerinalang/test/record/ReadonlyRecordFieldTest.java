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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `readonly` record fields.
 *
 * @since 1.3.0
 */
public class ReadonlyRecordFieldTest {

    @Test
    public void testReadonlyRecordFields() {
        CompileResult result = BCompileUtil.compile("test-src/record/readonly_record_fields.bal");
        BRunUtil.invoke(result, "testReadonlyRecordFields");
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
        assertEquals(result.getErrorCount(), index);
    }
}
