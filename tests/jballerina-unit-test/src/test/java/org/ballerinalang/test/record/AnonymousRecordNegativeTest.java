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
import static org.ballerinalang.test.BAssertUtil.validateWarning;

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
        Assert.assertEquals(negativeResult.getErrorCount(), 52);
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
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 26, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 27, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 28, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 32, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 33, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 34, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 35, 26);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 36, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 37, 37);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 37, 57);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 38, 43);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 39, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 40, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 41, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 44, 42);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 51, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 56, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 60, 23);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 61, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 62, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 63, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 64, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 65, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 66, 44);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 67, 45);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 68, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 68, 69);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 69, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 70, 31);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 71, 31);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 76, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 81, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 85, 33);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 85, 73);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 86, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 87, 32);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 88, 32);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 89, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 90, 51);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'string'", 91, 33);
        validateError(negativeResult, index, "incompatible types: expected 'int', found 'string'", 92, 37);
    }

    @Test(description = "Test record with closure type mutability negative scenarios")
    public void testRecordWithClosureTypeMutabilityNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/record/anon_record_isolation_analysis_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 58);
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
        validateWarning(compileResult, index++, "unused variable 'a9'", 28, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 28, 27);
        validateWarning(compileResult, index++, "unused variable 'a10'", 29, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 29, 30);
        validateWarning(compileResult, index++, "unused variable 'a11'", 30, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 30, 30);
        validateWarning(compileResult, index++, "unused variable 'a12'", 31, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 31, 30);
        validateWarning(compileResult, index++, "unused variable 'a1'", 37, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 37, 23);
        validateWarning(compileResult, index++, "unused variable 'a2'", 38, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 38, 23);
        validateWarning(compileResult, index++, "unused variable 'a3'", 39, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 39, 24);
        validateWarning(compileResult, index++, "unused variable 'a4'", 40, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 40, 29);
        validateWarning(compileResult, index++, "unused variable 'a5'", 41, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 41, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 41, 47);
        validateWarning(compileResult, index++, "unused variable 'a6'", 42, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 42, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 42, 47);
        validateWarning(compileResult, index++, "unused variable 'a7'", 43, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 43, 23);
        validateWarning(compileResult, index++, "unused variable 'a8'", 44, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 44, 28);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 44, 55);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 44, 83);
        validateWarning(compileResult, index++, "unused variable 'a9'", 45, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 45, 29);
        validateWarning(compileResult, index++, "unused variable 'a10'", 46, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 46, 29);
        validateWarning(compileResult, index++, "unused variable 'a11'", 47, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 47, 29);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 52, 42);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 59, 23);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 64, 23);
        validateWarning(compileResult, index++, "unused variable 'a1'", 69, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 69, 23);
        validateWarning(compileResult, index++, "unused variable 'a2'", 70, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 70, 24);
        validateWarning(compileResult, index++, "unused variable 'a3'", 71, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 71, 24);
        validateWarning(compileResult, index++, "unused variable 'a4'", 72, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 72, 24);
        validateWarning(compileResult, index++, "unused variable 'a5'", 73, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 73, 27);
        validateWarning(compileResult, index++, "unused variable 'a6'", 74, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 74, 29);
        validateWarning(compileResult, index++, "unused variable 'a7'", 75, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 75, 44);
        validateWarning(compileResult, index++, "unused variable 'a8'", 76, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 76, 45);
        validateWarning(compileResult, index++, "unused variable 'a9'", 77, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 77, 28);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 77, 65);
        validateWarning(compileResult, index++, "unused variable 'a10'", 78, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 78, 30);
        validateWarning(compileResult, index++, "unused variable 'a11'", 79, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 79, 31);
        validateWarning(compileResult, index++, "unused variable 'a12'", 80, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 80, 31);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 85, 27);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 90, 27);
        validateWarning(compileResult, index++, "unused variable 'a1'", 94, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 94, 33);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 94, 69);
        validateWarning(compileResult, index++, "unused variable 'a2'", 95, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 95, 29);
        validateWarning(compileResult, index++, "unused variable 'a3'", 96, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 96, 32);
        validateWarning(compileResult, index++, "unused variable 'a4'", 97, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 97, 32);
        validateWarning(compileResult, index++, "unused variable 'a5'", 98, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 98, 30);
        validateWarning(compileResult, index++, "unused variable 'a6'", 99, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 99, 51);
        validateWarning(compileResult, index++, "unused variable 'a7'", 100, 5);
        validateError(compileResult, index++, "invalid access of mutable storage in the default value " +
                "of a record field", 100, 33);
        validateWarning(compileResult, index++, "unused variable 'a8'", 101, 5);
        validateError(compileResult, index, "invalid access of mutable storage in the default value " +
                "of a record field", 101, 37);
    }
}
