/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for Function pointers and lambda.
 *
 * @since 0.90
 */
public class FunctionPointersNegativeTest {

    @Test()
    public void testFunctionPointerAsVariable() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-type-mismatch1-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'function (string,int) returns " +
                "(boolean)', found 'function (string,float) returns (boolean)'", 2, 53);
    }

    @Test()
    public void testLambdaAsVariable() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-type-mismatch2-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'function (string,int) returns " +
                "(boolean)', found 'function (string,boolean) returns (boolean)'", 2, 53);
    }

    @Test()
    public void testFPInStruct() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/negative/fp-struct-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "function invocation on type '(anydata|error)' is not supported", 17, 16);
    }

    @Test()
    public void testFPInStructIncorrectArg() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-struct-incorrect-arg-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found 'Person'", 32, 44);
    }

    @Test()
    public void testFPWithNoImport() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-with-import-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 5);
        int i = -1;
        BAssertUtil.validateError(result, ++i, "undefined module 'streams'", 19, 5);
        BAssertUtil.validateError(result, ++i, "unknown type 'Select'", 19, 5);
        BAssertUtil.validateError(result, ++i, "undefined module 'streams'", 19, 29);
        BAssertUtil.validateError(result, ++i, "undefined function 'createSelect'", 19, 29);
        BAssertUtil.validateError(result, ++i, "undefined symbol 'outputProcess'", 19, 50);
//        BAssertUtil.validateError(result, ++i, "array index out of range: index: '3', size: '2'", 23, 29); #12122
    }

    @Test()
    public void testFPInvalidInvocation() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/negative" +
                "/fp_invalid_invocation_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 10);
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined function 'Person.getFullName'", 35, 16);
        BAssertUtil.validateError(result, i++, "undefined function 'Person.getName'", 36, 16);
        BAssertUtil.validateError(result, i++, "undefined function 'f1'", 39, 5);
        BAssertUtil.validateError(result, i++, "undefined function 'f2'", 42, 9);
        BAssertUtil.validateError(result, i++, "undefined field 'getFname' in object 'Employee'", 45, 14);
        BAssertUtil.validateError(result, i++, "undefined function 'f3'", 46, 9);
        BAssertUtil.validateError(result, i++, "undefined function 'f4'", 49, 9);
        BAssertUtil.validateError(result, i++, "undefined function 'getLname' in object 'Employee'", 51, 9);
        BAssertUtil.validateError(result, i++, "undefined field 'getFname' in object 'Employee'", 76, 14);
        BAssertUtil.validateError(result, i++, "undefined field 'getFname' in object 'Employee'", 85, 9);
    }
}
