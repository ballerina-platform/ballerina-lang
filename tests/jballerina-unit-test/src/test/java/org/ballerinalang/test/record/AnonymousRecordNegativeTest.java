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
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/record/anon_record_semantic_analysis_negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 33);
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
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 35, 43);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 38, 42);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 45, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 50, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 54, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 55, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 56, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 57, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 58, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 59, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 60, 44);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 61, 45);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 62, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 62, 69);
        validateError(negativeResult, index, "incompatible types: expected 'int', found 'string'", 67, 27);
    }

    @Test(description = "Test record with closure type mutability negative scenarios")
    public void testRecordWithClosureTypeMutabilityNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/record/anon_record_isolation_analysis_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 39);
        int index = 0;
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 20, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 21, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 22, 24);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 23, 29);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 24, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 24, 47);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 25, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 25, 47);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 26, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 27, 28);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 27, 55);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 27, 83);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 28, 27);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 34, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 35, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 36, 24);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 37, 29);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 38, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 38, 47);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 39, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 39, 47);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 40, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 41, 28);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 41, 55);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 41, 83);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 46, 42);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 53, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 58, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 63, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 64, 24);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 65, 24);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 66, 24);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 67, 27);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 68, 29);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 69, 44);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 70, 45);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 71, 28);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 71, 65);
        validateError(compileResult, index, "invalid access of mutable storage in the default value " +
                "of a record field", 76, 27);
    }
}
