/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.function;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina `function` type.
 *
 *  @since 2.0.0
 */
public class AnyFunctionTypeTest {
    private CompileResult functionTypeTestResult;

    @BeforeClass
    public void setup() {
        functionTypeTestResult = BCompileUtil.compile("test-src/types/function/test_any_function_type.bal");
    }

    @Test
    public void testAnyFunctionTypeNegative() {
        CompileResult negativeCompileResult =
                BCompileUtil.compile("test-src/types/function/test_any_function_type_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function', found 'string'", 31, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'isolated function', found 'function (int) returns (int)'", 35, 30);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "'transactional' qualifier not allowed", 39, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot call function pointer of type 'function'", 44, 41);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot call function pointer of type 'function'", 45, 42);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot infer types of the arrow expression with unknown invokable type", 49, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function () returns (string)', found 'function () " +
                        "returns ((int|never))'", 53, 37);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function', found 'int'", 62, 30);
        Assert.assertEquals(negativeCompileResult.getErrorCount(), i);
    }

    @Test
    public void testFunctionWithFunctionReturnType() {
        BRunUtil.invoke(functionTypeTestResult, "testFunctionWithFunctionReturnType");
    }

    @Test
    public void testRecordWithFunctionTypeField() {
        BRunUtil.invoke(functionTypeTestResult, "testRecordWithFunctionTypeField");
    }

    @Test
    public void testFunctionAsMappingTypeParam() {
        BRunUtil.invoke(functionTypeTestResult, "testFunctionAsMappingTypeParam");
    }

    @Test
    public void testFunctionWithUnionType() {
        BRunUtil.invoke(functionTypeTestResult, "testFunctionWithUnionType");
    }

    @Test
    public void testObjectWithFunctionTypeField() {
        BRunUtil.invoke(functionTypeTestResult, "testObjectWithFunctionTypeField");
    }

    @Test
    public void testReferringToFunctionWithAnyFunctionReturnType() {
        BRunUtil.invoke(functionTypeTestResult, "testReferringToFunctionWithAnyFunctionReturnType");
    }

    @Test
    public void testCastingToFunctionWithAnyFunctionReturnType() {
        BRunUtil.invoke(functionTypeTestResult, "testCastingToFunctionWithAnyFunctionReturnType");
    }

    @Test
    public void testRuntimeHashCodeViaFunctionEquality() {
        BRunUtil.invoke(functionTypeTestResult, "testRuntimeHashCodeViaFunctionEquality");
    }

    @Test
    public void testFunctionWithNeverOrNeverEqualReturnType() {
        BRunUtil.invoke(functionTypeTestResult, "testFunctionWithNeverOrNeverEqualReturnType");
    }
}
