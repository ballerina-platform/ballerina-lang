/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.record;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Negative test cases for anonymous records.
 *
 * @since 2.0.0
 */
public class AnonymousRecordNegativeTest {

    @Test(description = "Test local anonymous records that are part of another type")
    public void testAnonRecordsNegativeScenarios() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/record/anon_record_negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 19);
        int index = 0;

        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 18, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 19, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 20, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 21, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 22, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 23, 37);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 23, 58);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 24, 44);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 25, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 25, 60);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 25, 93);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 29, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 30, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 31, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 32, 26);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 33, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 34, 37);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 34, 57);
        validateError(negativeResult, index, "incompatible types: expected 'int', found 'string'", 35, 43);
    }
}
