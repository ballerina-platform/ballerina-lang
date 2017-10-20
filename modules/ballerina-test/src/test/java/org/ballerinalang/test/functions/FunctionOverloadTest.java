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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test overloading nature of functions.
 */
public class FunctionOverloadTest {

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test function overloading which has different argument counts")
    public void testFunctionOverloadingDifferentArgCount() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/function-overloading-diff-arg-count.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, "redeclared symbol 'testOverloading'", 5, 1);
    }

    @Test(description = "Test functino overloading which has same argument count")
    public void testFunctionOverloadingSameArgCountTest() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/function-overloading.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, "redeclared symbol 'testOverloading'", 5, 1);
    }

    @Test(description = "Test if incorrect function overloading produces errors")
    public void testInvalidFunctionOverloading() {
        CompileResult compile = BCompileUtil.compile("test-src/functions/invalid-function-overloading.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, "redeclared symbol 'testOverloading'", 5, 1);
    }

}
