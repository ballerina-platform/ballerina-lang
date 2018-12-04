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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Negative test cases for open records.
 *
 * @since 0.982.1
 */
public class OpenRecordNegativeTest {

    @Test(description = "Test use of undefined types as the rest field type", enabled = false)
    public void testUndefinedTypeAsRestFieldType() {
        CompileResult result = BCompileUtil.compile("test-src/record/undefined_rest_field.bal");
        assertEquals(result.getErrorCount(), 1);
    }

    @Test(description = "Test invalid rest field type")
    public void testInvalidRestField() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_negative.bal");

        assertEquals(result.getErrorCount(), 5);

        String expectedErrMsg = "incompatible types: expected 'string', ";
        validateError(result, 0, expectedErrMsg + "found 'int'", 8, 45);
        validateError(result, 1, expectedErrMsg + "found 'boolean'", 8, 57);
        validateError(result, 2, "invalid usage of record literal with type 'anydata'", 17, 36);
        validateError(result, 3, "unknown type 'Animal'", 21, 5);
        validateError(result, 4, "incompatible types: expected 'anydata', found 'Bar'", 30, 18);
    }

    @Test(description = "Test white space between the type name and ellipsis in rest descriptor")
    public void testRestDescriptorSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_invalid_rest_desc.bal");

        validateError(result, 0, "invalid record rest descriptor", 5, 12);
        validateError(result, 1, "invalid record rest descriptor", 12, 14);
        validateError(result, 2, "invalid record rest descriptor", 20, 5);
    }

    @Test(description = "Test record literal with repeated keys")
    public void testDuplicatedKeysInRecordLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/open_record_duplicated_key.bal");
        validateError(compileResult, 0, "invalid usage of record literal: " +
                "duplicate key 'noOfChildren'", 8, 58);
    }

    @Test(description = "Test function invocation on a nil-able function pointer")
    public void testNilableFuncPtrInvocation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/open_record_nil-able_fn_ptr.bal");
        validateError(compileResult, 0, "incompatible types: expected 'string', found 'string?'", 28, 16);
        validateError(compileResult, 1, "incompatible types: expected 'string', found 'string?'", 33, 16);
    }

    @Test(description = "Test ambiguity resolution")
    public void testAmbiguityResolution() {
        CompileResult result = BCompileUtil.compile("test-src/record/negative/open_record_ambiguity.bal");
        assertEquals(result.getErrorCount(), 8);
        int index = 0;
        validateError(result, index++, "ambiguous type 'InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig'", 36,
                      22);
        validateError(result, index++, "ambiguous type 'InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig'", 37,
                      22);
        validateError(result, index++, "ambiguous type 'InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig'", 38,
                      22);
        validateError(result, index++, "ambiguous type 'A|B|C'", 70, 25);
        validateError(result, index++, "ambiguous type 'A|B|C'", 71, 25);
        validateError(result, index++, "ambiguous type 'A|B|C'", 72, 25);
        validateError(result, index++, "ambiguous type 'A|B|C'", 73, 25);
        validateError(result, index, "unnecessary condition: expression will always evaluate to 'true'", 78, 9);
    }

    @Test(description = "Test uninitialized record access")
    public void testUninitRecordAccess() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/open_record_uninit_access.bal");
        assertEquals(compileResult.getErrorCount(), 15);
        int index = 0;
        validateError(compileResult, index++, "variable 'publicPerson' is not initialized", 22, 1);
        validateError(compileResult, index++, "variable 'p' is not initialized", 27, 19);
        validateError(compileResult, index++, "variable 'p' is not initialized", 28, 12);
        validateError(compileResult, index++, "variable 'p' is not initialized", 30, 5);
        validateError(compileResult, index++, "variable 'p' is not initialized", 31, 5);
        validateError(compileResult, index++, "variable 'p' is not initialized", 33, 42);
        validateError(compileResult, index++, "variable 'publicPerson' is not initialized", 37, 12);
        validateError(compileResult, index++, "variable 'publicPerson' is not initialized", 38, 12);
        validateError(compileResult, index++, "variable 'publicPerson' is not initialized", 40, 5);
        validateError(compileResult, index++, "variable 'publicPerson' is not initialized", 41, 5);
        validateError(compileResult, index++, "variable 'globalPerson' is not initialized", 43, 12);
        validateError(compileResult, index++, "variable 'globalPerson' is not initialized", 44, 12);
        validateError(compileResult, index++, "variable 'globalPerson' is not initialized", 46, 5);
        validateError(compileResult, index++, "variable 'globalPerson' is not initialized", 47, 5);
        validateError(compileResult, index, "variable 'p4' is not initialized", 67, 12);
    }
}
