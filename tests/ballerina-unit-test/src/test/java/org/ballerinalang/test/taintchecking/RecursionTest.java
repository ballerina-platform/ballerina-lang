/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test recursion conditions (direct recursions and cyclic invocations of functions), that will be addressed by the
 * the conflict resolution mechanism of taint analyzer.
 */
public class RecursionTest {
    // Test recursions.

    @Test
    public void testRecursiveFunctionCallingSensitiveFunction() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "recursive-function-calling-sensitive-function.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testRecursiveFunctionCallingSensitiveFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "recursive-function-calling-sensitive-function-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'inputData'", 2, 21);
    }

    @Test
    public void testRecursiveFunctionAlteringSensitiveStatusCallingSensitiveFunction1Negative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "cyclic-call-altering-sensitive-status-calling-sensitive-function-1-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 12, 20);
    }

    @Test
    public void testRecursiveFunctionAlteringSensitiveStatusCallingSensitiveFunction2Negative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "cyclic-call-altering-sensitive-status-calling-sensitive-function-2-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 18, 20);
    }

    @Test
    public void testMultipleRecursionsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/multiple-recursions.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 17, 20);
    }
}
