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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.function;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for function pointers negative tests.
 */
public class FunctionPointerInvocationNegativeTest {

    @Test
    public void testFPNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/function/function-pointers-negative.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 3);
        BAssertUtil.validateError(compileResult, 0, "undefined function 'pow'", 3, 12);
        BAssertUtil.validateError(compileResult, 1, "undefined function 'pow' in object 'Test1'", 8, 12);
        BAssertUtil.validateError(compileResult, 2, "undefined function 'pow' in object 'Test1'", 13, 12);
    }
}
