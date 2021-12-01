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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Negative tests for finite types.
 *
 * @since 0.990.0
 */
public class FiniteTypeNegativeTest {

    @Test()
    public void finiteAssignmentStateType() {
        CompileResult result = BCompileUtil.compile("test-src/types/finite/func_type_labeling_negative.bal");
        validateError(result, 0, "incompatible types: expected 'FuncType', found 'function (int) returns " +
                "(int)'", 20, 19);
        validateError(result, 1, "incompatible types: expected 'string', found 'int'", 23, 16);
        validateError(result, 2, "incompatible types: expected 'FuncType', found 'function (string) " +
                "returns (string)'", 27, 19);
    }

    @Test()
    public void testInvalidLiteralAssignment() {

        CompileResult result = BCompileUtil.compile("test-src/types/finite/finite_type_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 17, "Error count mismatch");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'Finite', found 'string'", 33, 16);
        validateError(result, i++, "incompatible types: expected 'ByteType', found '5'", 40, 18);
        validateError(result, i++, "incompatible types: expected 'IntType', found '5'", 47, 17);
        validateError(result, i++, "incompatible types: expected 'IntType', found 'float'", 52, 17);
        validateError(result, i++, "incompatible types: expected 'FloatType', found '5'", 59, 19);
        validateError(result, i++, "incompatible types: expected 'FloatType', found '5d'", 64, 19);
        validateError(result, i++, "incompatible types: expected 'DecimalType', found '5'", 71, 21);
        validateError(result, i++, "incompatible types: expected 'DecimalType', found '5f'", 76, 21);
        validateError(result, i++, "incompatible types: expected 'IntType', found 'int'", 81, 17);
        validateError(result, i++, "incompatible types: expected 'string', found 'StringOrIntVal'", 89, 17);
        validateError(result, i++, "incompatible types: expected 'int', found 'StringOrInt'", 92, 14);
        validateError(result, i++, "incompatible types: expected 't3', found 'float'", 102, 13);
        validateError(result, i++, "incompatible types: expected '(t|t2)', found 'decimal'", 107, 14);
        validateError(result, i++, "incompatible types: expected 'Foo', found 'int'", 116, 14);
        validateError(result, i++, "incompatible types: expected 'Foo2', found 'int'", 117, 15);
        validateError(result, i++, "incompatible types: expected 'Foo4', found 'int'", 118, 15);
        validateError(result, i, "incompatible types: expected 'chiran', found 'int'", 119, 18);
    }
}
