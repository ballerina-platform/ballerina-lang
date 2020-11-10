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
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'function (string,int) returns " +
                "(boolean)', found 'function (string,float) returns (boolean)'", 2, 53);
        BAssertUtil.validateError(result, 1, "unknown type 'Context'", 10, 29);
        BAssertUtil.validateError(result, 2, "unknown type 'FunctionEntry'", 12, 5);
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
        BAssertUtil.validateError(result, 0, "undefined field 'getFullName' in record 'Person'", 17, 20);
    }

    @Test()
    public void testFPInStructIncorrectArg() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-struct-incorrect-arg-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found 'Person'", 32, 39);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testFPWithNoImport() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-with-import-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        int i = -1;
        BAssertUtil.validateError(result, ++i, "undefined module 'streams'", 19, 5);
        BAssertUtil.validateError(result, ++i, "unknown type 'Select'", 19, 5);
        BAssertUtil.validateError(result, ++i, "undefined function 'createSelect'", 19, 32);
        BAssertUtil.validateError(result, ++i, "undefined module 'streams'", 19, 32);
        BAssertUtil.validateError(result, ++i, "undefined symbol 'outputProcess'", 19, 53);
//        BAssertUtil.validateError(result, ++i, "array index out of range: index: '3', size: '2'", 23, 29); #12122
    }

    @Test()
    public void testFPInvalidInvocation() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/negative" +
                "/fp_invalid_invocation_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined field 'getFullName' in record 'Person'", 35, 20);
        BAssertUtil.validateError(result, i, "undefined method 'getLname' in object 'Employee'", 83, 11);
    }

    @Test
    public void testFPWithMissingArgs() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/negative" +
                                                    "/fp_invocation_with_missing_args.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(result, i++, "missing required parameter 'i' in call to 'fn'()", 9, 16);
        BAssertUtil.validateError(result, i++, "missing required parameter 'i' in call to 'fn'()", 20, 16);
        BAssertUtil.validateError(result, i++, "too many arguments in call to 'fn()'", 31, 16);
        BAssertUtil.validateError(result, i, "too many arguments in call to 'fn()'", 42, 16);
    }
}
