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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;

/**
 * Negative test cases for open records.
 *
 * @since 0.982.1
 */
public class OpenRecordNegativeTest {

    @Test(description = "Test use of undefined types as the rest field type")
    public void testUndefinedTypeAsRestFieldType() {
        CompileResult result = BCompileUtil.compile("test-src/record/undefined_rest_field.bal");
        assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "unknown type 'UndefinedType'", 4, 5);
    }

    @Test(description = "Test invalid rest field type")
    public void testInvalidRestField() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_negative.bal");
        String expectedErrMsg = "incompatible types: expected 'string', ";
        int indx = 0;

        validateError(result, indx++, expectedErrMsg + "found 'int'", 8, 51);
        validateError(result, indx++, expectedErrMsg + "found 'boolean'", 8, 66);
        validateError(result, indx++, "ambiguous type '(anydata|json)'", 18, 40);
        validateError(result, indx++, "unknown type 'Animal'", 22, 5);
        validateError(result, indx++, "incompatible types: expected '(anydata|json)', found 'Bar'", 31, 21);
        validateError(result, indx++, "incompatible types: expected 'anydata', found 'error'", 49, 17);
        validateError(result, indx++, "incompatible types: expected 'anydata', found 'MyError'", 50, 17);
        validateError(result, indx++, "incompatible types: expected 'anydata', found 'error'", 53, 15);
        validateError(result, indx++, "incompatible types: expected 'anydata', found 'MyError'", 54, 15);
        validateError(result, indx++,
                      "invalid operation: type 'Person' does not support optional field access for field 'firstName'",
                      59, 26);
        validateError(result, indx++,
                "undefined field 'toValue' in record 'Teacher'",
                68, 27);
        assertEquals(result.getErrorCount(), indx);
    }

    @Test(description = "Test invalid record assignment error message")
    public void invalidRecordAssignment() {
        CompileResult result = BCompileUtil.compile("test-src/record/record-assignment-negative.bal");
        validateError(result, 0, "incompatible types: expected 'record {| int i; record {| string name;" +
                " anydata...; |} j; anydata...; |}', found 'int'", 4, 9);
    }

    @Test(description = "Test white space between the type name and ellipsis in rest descriptor",
            groups = { "disableOnOldParser" })
    public void testRestDescriptorSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_invalid_rest_desc.bal");
        assertEquals(result.getErrorCount(), 0);
    }

    @Test(description = "Test function invocation on a nil-able function pointer")
    public void testNilableFuncPtrInvocation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/open_record_nil-able_fn_ptr.bal");
        String errMsg =
                "invalid method call expression: expected a function type, but found 'function" +
                        " (string,string) returns (string)?'";
        int indx = 0;

        validateError(compileResult, indx++, errMsg, 28, 17);
        validateError(compileResult, indx++, errMsg, 33, 17);
        validateError(compileResult, indx++, errMsg, 47, 17);
        validateError(compileResult, indx++, errMsg, 53, 17);
        assertEquals(compileResult.getErrorCount(), indx);
    }

    @Test(description = "Test ambiguity resolution")
    public void testAmbiguityResolution() {
        CompileResult result = BCompileUtil.compile("test-src/record/negative/open_record_ambiguity.bal");
        assertEquals(result.getErrorCount(), 5);
        int index = 0;
        validateError(result, index++, "ambiguous type '(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig)'", 37,
                      24);
        validateError(result, index++, "ambiguous type '(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig)'", 38,
                      24);
        validateError(result, index++, "ambiguous type '(A|B|C)'", 72, 25);
        validateError(result, index++, "ambiguous type '(A|B|C)'", 73, 25);
        validateError(result, index, "ambiguous type '(A|B|C)'", 74, 25);
        // validateError(result, index, "unnecessary condition: expression will always evaluate to 'true'", 78, 9);
    }

    @Test(description = "Test uninitialized record access")
    public void testUninitRecordAccessSemanticsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative" +
                "/open_record_uninit_access_semantics_negative.bal");
        int index = 0;
        validateError(compileResult, index++, "operator '?:' cannot be applied to type 'string'", 25, 12);
        assertEquals(compileResult.getErrorCount(), index);
    }

    @Test(description = "Test uninitialized record access")
    public void testUninitRecordAccess() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/negative/open_record_uninit_access.bal");
        int index = 0;
        validateWarning(compileResult, index++, "unused variable 'name'", 24, 5);
        validateError(compileResult, index++, "variable 'p' is not initialized", 24, 19);
        validateError(compileResult, index++, "variable 'p' is not initialized", 27, 5);
        validateError(compileResult, index++, "variable 'p' is not initialized", 28, 5);
        validateError(compileResult, index++, "variable 'p' is not initialized", 30, 41);
        validateError(compileResult, index++, "variable 'p4' is not initialized", 52, 12);
        assertEquals(compileResult.getErrorCount(), index - 1);
        assertEquals(compileResult.getWarnCount(), 1);
    }
}
