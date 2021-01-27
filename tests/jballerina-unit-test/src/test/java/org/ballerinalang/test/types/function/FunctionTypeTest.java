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
 */
public class FunctionTypeTest {
    private CompileResult functionTypeTestResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        functionTypeTestResult = BCompileUtil.compile("test-src/types/function/function-type.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/types/function/function-type-negative.bal");
    }

    @Test
    public void testNeverTypeNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function', found 'string'", 31, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'isolated function', found 'function (int) returns (int)'", 35, 30);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "'transactional' qualifier not allowed", 39, 28);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "function pointer with function type cannot be invoked", 49, 39);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function (int) returns (string)', found 'other'", 49, 39);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "function pointer with function type cannot be invoked", 50, 40);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'function (int) returns (string)', found 'other'", 50, 40);

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
}
