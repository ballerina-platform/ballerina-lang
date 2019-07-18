/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.functions;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test function signatures and calling with optional and named params.
 */
public class FunctionSignatureNegativeTest {

    @Test
    public void testNegativeFuncSignature() {
        int i = 0;
        CompileResult result = BCompileUtil.compile("test-src/functions/different-function-signatures-negative.bal");
        String tooManyArguments = "too many arguments in call to ";
        String notEnoughArguments = "not enough arguments in call to ";

        BAssertUtil.validateError(result, i++, "redeclared symbol 'c'", 1, 73);
        BAssertUtil.validateError(result, i++, "redeclared argument 'a'", 17, 19);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'c'", 21, 19);
        BAssertUtil.validateError(result, i++, "invalid rest arguments", 29, 23);
        BAssertUtil.validateError(result, i++, "this function must return a result", 32, 1);
        BAssertUtil.validateError(result, i++, "invalid rest arguments", 33, 23);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'json', found 'xml'", 40, 61);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithOnlyPositionalParams()'", 57, 9);
        BAssertUtil.validateError(result, i++, "missing required parameter 'a' in call to " +
                "'functionWithOnlyPositionalParams'()", 57, 9);
        BAssertUtil.validateError(result, i++, "missing required parameter 'b' in call to " +
                "'functionWithOnlyPositionalParams'()", 57, 9);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                "'functionWithOnlyPositionalParams'()", 57, 9);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithOnlyPositionalParams()'", 58, 9);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                "'functionWithOnlyPositionalParams'()", 58, 9);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithOnlyPositionalParams()'", 59, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'string'", 59, 45);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                "'functionWithOnlyPositionalParams'()", 59, 9);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithOnlyPositionalParams()'", 60, 9);
        BAssertUtil.validateError(result, i++, "missing required parameter 'b' in call to " +
                "'functionWithOnlyPositionalParams'()", 60, 9);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyPositionalParams()'", 61, 9);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyPositionalParams()'", 62, 56);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyPositionalParams()'", 63, 62);
        BAssertUtil.validateError(result, i++, "redeclared argument 'a'", 63, 45);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                "'functionWithOnlyPositionalParams'()", 63, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'string'", 68, 46);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyDefaultableParams()'", 70, 9);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyDefaultableParams()'", 71, 57);
        BAssertUtil.validateError(result, i++, tooManyArguments + "'functionWithOnlyDefaultableParams()'", 72, 63);
        BAssertUtil.validateError(result, i++, "redeclared argument 'a'", 72, 46);
        BAssertUtil.validateError(result, i++, "required parameter not allowed after defaultable parameters", 75, 60);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'boolean[]'", 85, 33);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'float', found 'boolean[]'", 87, 28);
        BAssertUtil.validateError(result, i++, "positional argument not allowed after named arguments", 89, 45);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'boolean[]'", 89, 45);
        BAssertUtil.validateError(result, i++, "rest argument not allowed after named arguments", 90, 45);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithNoRestParam()'", 98, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 98, 5);
        BAssertUtil.validateError(result, i++, notEnoughArguments + "'functionWithNoRestParam()'", 99, 5);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'int', found 'string'", 99, 29);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 99, 5);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'int', found 'string'", 100, 29);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'string', found 'int'", 100, 39);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'float', found 'boolean'", 103, 44);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'float', found 'boolean'", 105, 44);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'float'", 105, 51);
        BAssertUtil.validateError(result, i++, "not enough arguments in call to " +
                "'functionWithNoRestParam()'", 106, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'x' in call to " +
                "'functionWithNoRestParam'()", 106, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 106, 5);
        BAssertUtil.validateError(result, i++, "not enough arguments in call to " +
                "'functionWithNoRestParam()'", 107, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'x' in call to " +
                "'functionWithNoRestParam'()", 107, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 107, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'x' in call to " +
                "'functionWithNoRestParam'()", 108, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 108, 5);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 114, 5);
        BAssertUtil.validateError(result, i++, "too many arguments in call to 'getFloat()'", 115, 36);
        BAssertUtil.validateError(result, i++, "missing required parameter 'y' in call to " +
                "'functionWithNoRestParam'()", 115, 5);

        Assert.assertEquals(i, result.getErrorCount());
    }

    @Test
    public void testFuncWithTwoRestParams() {
        CompileResult result = BCompileUtil.compile("test-src/functions/function-with-two-rest-params.bal");
        BAssertUtil.validateError(result, 0, "mismatched input ','. expecting ')'", 1, 52);
    }

    @Test
    public void testOutsideObjectMethodDefinition() {
        CompileResult result = BCompileUtil.compile("test-src/functions" +
                "/function_with_outside_object_method_definition.bal");
        BAssertUtil.validateError(result, 0, "outside object method definitions are not allowed", 5, 1);
    }

    @Test
    public void testExternalResourceFunction() {
        CompileResult result = BCompileUtil.compile("test-src/functions/extern_resource_function_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "external resource functions are not supported by the implementation",
                                  19, 5);
        BAssertUtil.validateError(result, 1, "external resource functions are not supported by the implementation",
                                  21, 5);
        BAssertUtil.validateError(result, 2, "external resource functions are not supported by the implementation",
                                  23, 5);
    }
}
