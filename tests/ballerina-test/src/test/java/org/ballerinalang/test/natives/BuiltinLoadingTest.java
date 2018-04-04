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
package org.ballerinalang.test.natives;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Testing Ballerina.builtin related generic test cases.
 */
public class BuiltinLoadingTest {

    @Test
    public void testBuiltinImport() {
        CompileResult result = BCompileUtil.compile("test-src/natives/builtin-loading-negative.bal");
        BAssertUtil.validateError(result, 0, "cannot resolve package 'ballerina.builtin'", 1, 1);
    }

    @Test
    public void testBuiltinCoreImport() {
        CompileResult result = BCompileUtil.compile("test-src/natives/builtin-core-loading-negative.bal");
        BAssertUtil.validateError(result, 0, "cannot resolve package 'ballerina.builtin.core'", 1, 1);
    }


    @Test
    public void testRedeclaredSymbols() {
        CompileResult result = BCompileUtil.compile("test-src/natives/builtin-symbol-negative.bal");
        BAssertUtil.validateError(result, 0, "break cannot be used outside of a loop", 2, 5);
        BAssertUtil.validateError(result, 1, "redeclared builtin symbol 'error'", 5, 1);
        BAssertUtil.validateError(result, 2, "redeclared builtin symbol 'error'", 10, 5);
        BAssertUtil.validateError(result, 3, "function 'getMessage' defined on non-local type 'error'", 13, 11);
    }
}
