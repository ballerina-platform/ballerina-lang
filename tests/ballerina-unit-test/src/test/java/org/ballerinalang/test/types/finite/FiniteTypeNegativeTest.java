/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.finite;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Negative tests for finite types.
 *
 * @since 0.990.0
 */
public class FiniteTypeNegativeTest {

    @Test()
    public void finiteAssignmentStateType() {
        CompileResult result = BCompileUtil.compile("test-src/types/finite/func_type_labeling_negative.bal");
        validateError(result, 0,
                      "incompatible types: expected 'function (string) returns (int)', found 'function (int) returns " +
                              "(int)'", 20, 19);
        validateError(result, 1, "incompatible types: expected 'string', found 'int'", 23, 21);
        validateError(result, 2,
                      "incompatible types: expected 'function (string) returns (int)', found 'function (string) " +
                              "returns (string)'", 27, 19);
    }

    @Test()
    public void testInvalidLiteralAssignment() {
        CompileResult result = BCompileUtil.compile("test-src/types/finite/finite_type_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 11, "Error count mismatch");

        validateError(result, 0, "incompatible types: expected '5|100', found 'string'", 33, 16);
        validateError(result, 1, "incompatible types: expected '5', found '5'", 40, 18);
        validateError(result, 2, "incompatible types: expected '5', found '5'", 47, 17);
        validateError(result, 3, "incompatible types: expected '5', found 'float'", 52, 17);
        validateError(result, 4, "incompatible types: expected '5.0', found '5'", 59, 19);
        validateError(result, 5, "incompatible types: expected '5.0', found '5'", 64, 19);
        validateError(result, 6, "incompatible types: expected '5', found '5'", 71, 21);
        validateError(result, 7, "incompatible types: expected '5', found '5.0'", 76, 21);
        validateError(result, 8, "incompatible types: expected '5', found 'int'", 81, 17);
        validateError(result, 9, "incompatible types: expected 'string', found 'foo|1'", 89, 17);
        validateError(result, 10, "incompatible types: expected 'int', found 'string|int'", 92, 14);
    }
}
