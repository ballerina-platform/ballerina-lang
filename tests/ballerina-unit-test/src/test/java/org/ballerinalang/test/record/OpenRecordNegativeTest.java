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

package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for open records.
 *
 * @since 0.982.1
 */
public class OpenRecordNegativeTest {

    @Test(description = "Test use of undefined types as the rest field type", enabled = false)
    public void testUndefinedTypeAsRestFieldType() {
        CompileResult result = BCompileUtil.compile("test-src/record/undefined_rest_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }

    @Test(description = "Test invalid rest field type")
    public void testInvalidRestField() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_negative.bal");

        Assert.assertEquals(result.getErrorCount(), 4);

        String expectedErrMsg = "incompatible types: expected 'string', ";
        BAssertUtil.validateError(result, 0, expectedErrMsg + "found 'int'", 8, 45);
        BAssertUtil.validateError(result, 1, expectedErrMsg + "found 'boolean'", 8, 57);
        BAssertUtil.validateError(result, 2, "invalid usage of record literal with type 'any'", 17, 36);
        BAssertUtil.validateError(result, 3, "unknown type 'Animal'", 21, 5);
    }

    @Test(description = "Test white space between the type name and ellipsis in rest descriptor")
    public void testRestDescriptorSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_invalid_rest_desc.bal");

        BAssertUtil.validateError(result, 0, "invalid record rest descriptor", 5, 12);
        BAssertUtil.validateError(result, 1, "invalid record rest descriptor", 12, 14);
        BAssertUtil.validateError(result, 2, "invalid record rest descriptor", 20, 5);
    }

    @Test(description = "Test record literal with repeated keys")
    public void testDuplicatedKeysInRecordLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/open_record_duplicated_key.bal");
        BAssertUtil.validateError(compileResult, 0, "invalid usage of record literal: " +
                "duplicate key 'noOfChildren'", 8, 58);
    }

    @Test(description = "Test function invocation on a nil-able function pointer")
    public void testNilableFuncPtrInvocation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/open_record_nil-able_fn_ptr.bal");
        BAssertUtil.validateError(compileResult, 0,
                                  "invalid function pointer invocation on non-invokable field 'getName' in record " +
                                          "'Person'", 28, 16);
        BAssertUtil.validateError(compileResult, 1,
                                  "invalid function pointer invocation on non-invokable field 'getName' in record " +
                                          "'Person'", 33, 16);
    }
}
