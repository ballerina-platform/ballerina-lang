/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.record;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test record assignability.
 */
public class RecordAssignabilityTest {

    @Test
    public void testRecordAssignabilityNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/record/record_assignability_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; anydata...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; anydata...; |}'", 19, 55);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; anydata...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; anydata...; |}'", 20, 54);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; |}'", 25, 57);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; |}'", 26, 56);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean)...; |}'", 29, 58);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean)...; |}'", 30, 57);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; (int|boolean)...; |}'", 33, 72);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; (int|boolean)...; |}'", 34, 71);
        assertEquals(negativeResult.getErrorCount(), i);
    }
}
