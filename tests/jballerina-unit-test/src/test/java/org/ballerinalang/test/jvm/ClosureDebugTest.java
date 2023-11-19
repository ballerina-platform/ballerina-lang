/*
 *
 *   Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClosureDebugTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/closure-debug.bal");
    }

    @Test()
    public void testClosureDebug() {
        Assert.assertFalse(compileResult.getDiagnosticResult().hasErrors());
        Object returns = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(returns, 50L);
    }

    @Test()
    public void testClosureDebug1() {
        Assert.assertFalse(compileResult.getDiagnosticResult().hasErrors());
        Object returns = BRunUtil.invoke(compileResult, "test2");
        Assert.assertEquals(returns, 250L);
    }

    @Test()
    public void testClosureDebug2() {
        Assert.assertFalse(compileResult.getDiagnosticResult().hasErrors());
        Object returns = BRunUtil.invoke(compileResult, "test3");
        Assert.assertEquals(returns, 65L);
    }
}
