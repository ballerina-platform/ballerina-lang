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
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/recursion.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testRecursionWithinAttachedExternalFunctions() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/recursions/recursion-within-attached-external-function.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    // Test cyclic function invocations.

    @Test
    public void testCyclicCall() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/cyclic-call.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }
}
