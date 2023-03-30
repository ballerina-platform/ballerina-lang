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
package org.ballerinalang.test.functions;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test Custom function.
 */
public class CustomFunctionTest {

    @Test(description = "Test defining duplicate ballerina function")
    public void testDuplicateFunction() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/duplicate-function.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, "redeclared symbol 'foobar'", 5, 10);
    }

    @Test(description = "Test defining ballerina function with duplicate parameters")
    public void testDuplicateParameters() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/duplicate-parameters.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        // Checking duplicate parameter definition in a function starting at 35st column
        BAssertUtil.validateError(compile, 0, "redeclared symbol 'param'", 1, 35);
    }

    @Test
    public void testAssignValueToFunctionNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/assign_value_to_function_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 21, 5);
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 22, 5);
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 23, 6);
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 23, 11);
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 34, 13);
        BAssertUtil.validateError(compile, i++, "invalid assignment: 'function' declaration is final", 43, 26);
        Assert.assertEquals(compile.getErrorCount(), i);
    }

    @Test
    public void testAssignValueToObjectMethodNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/assign_value_to_object_method.bal");
        int i = 0;
        BAssertUtil.validateError(compile, i++, "cannot update 'final' object field 'getVal'", 30, 5);
        BAssertUtil.validateError(compile, i++, "cannot update 'final' object field 'getVal'", 33, 5);
        Assert.assertEquals(compile.getErrorCount(), i);
    }
}
