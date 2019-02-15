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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;

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
        Assert.assertEquals(result.getErrorCount(), 3, "Error count mismatch");

        validateError(result, 0, "incompatible types: expected '5|5|5', found 'float'", 32, 16);
        validateError(result, 1, "incompatible types: expected '5|100', found 'string'", 37, 16);
        validateError(result, 2, "incompatible types: expected '100.5|S', found 'float'", 42, 23);
    }
}
