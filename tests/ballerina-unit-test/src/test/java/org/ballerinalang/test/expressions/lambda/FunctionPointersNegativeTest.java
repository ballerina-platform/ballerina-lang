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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
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
        BAssertUtil.validateError(result, 0, "undefined field 'getFullName' in record 'Person'", 17, 16);

    }

    @Test()
    public void testFPInStructIncorrectArg() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/negative/fp-struct-incorrect-arg-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found 'Person'", 32, 39);
    }
}
