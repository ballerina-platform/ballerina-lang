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
 * Test conflicting conditions such as recursions and cyclic invocations of functions, that will be addressed by the
 * the conflict resolution mechanism of taint analyzer.
 *
 * @since 0.965.0
 */
public class ConflictTest {

    // Test recursions.

    @Test
    public void testRecursion() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/conflicts/recursion.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testRecursionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/conflicts/recursion-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0,
                "taint checking for 'f1' could not complete due to recursion with 'f1', add @tainted or " +
                "@untainted to returns", 3, 12);
    }

    @Test
    public void testRecursionWithinAttachedExternalFunctions() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/conflicts/recursion-within-attached-external-function.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testRecursionWithinAttachedExternalFunctionsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/conflicts/recursion-within-attached-external-function-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);

        BAssertUtil.validateError(result, 0,
                "taint checking for 'testFunction' could not complete due to recursion with 'TestObject.testFunction'" +
                        ", add @tainted or @untainted to returns", 7, 12);
        BAssertUtil.validateError(result, 1,
                "taint checking for 'main' could not complete due to recursion with 'TestObject.testFunction', add " +
                        "@tainted or @untainted to returns", 16, 26);
    }

    // Test cyclic function invocations.

    @Test
    public void testCyclicCall() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/conflicts/cyclic-call.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testCyclicCallNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/conflicts/cyclic-call-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 3);
        BAssertUtil.validateError(result, 0,
                "taint checking for 'f1' could not complete due to recursion with 'f2', add @tainted or " +
                "@untainted to returns", 2, 12);
        BAssertUtil.validateError(result, 1,
                "taint checking for 'f2' could not complete due to recursion with 'f3', add @tainted or " +
                "@untainted to returns", 6, 12);
        BAssertUtil.validateError(result, 2,
                "taint checking for 'f3' could not complete due to recursion with 'f1', add @tainted or " +
                        "@untainted to returns", 10, 12);
    }
}
