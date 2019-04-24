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
        BAssertUtil.validateError(result, i++, "redeclared symbol 'c'", 1, 66);

        BAssertUtil.validateError(result, i++, "redeclared argument 'a'", 17, 19);

        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'c'", 21, 19);

        BAssertUtil.validateError(result, i++, "invalid rest arguments", 29, 25);

        BAssertUtil.validateError(result, i++, "this function must return a result", 32, 1);

        BAssertUtil.validateError(result, i++, "invalid rest arguments", 33, 25);

        BAssertUtil.validateError(result, i++, "invalid value for parameter 'j': only simple literals allowed", 40,
                61);
        BAssertUtil.validateError(result, i, "incompatible types: expected 'json', found 'xml'", 40, 61);

    }

    @Test
    public void testFuncWithTwoRestParams() {
        CompileResult result = BCompileUtil.compile("test-src/functions/function-with-two-rest-params.bal");
        BAssertUtil.validateError(result, 0, "mismatched input ','. expecting ')'", 1, 52);
    }

    @Test
    public void testExternFunctionWithBody() {
        CompileResult result = BCompileUtil.compile("test-src/functions/extern-function-with-body.bal");
        BAssertUtil.validateError(result, 0, "external function 'foo' cannot have a body", 5, 1);
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
