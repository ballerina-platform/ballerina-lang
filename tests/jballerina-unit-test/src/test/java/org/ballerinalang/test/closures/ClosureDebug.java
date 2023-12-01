/*
 *
 *   Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.test.closures;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class ClosureDebug {

    @Test()
    public void test1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/closures/closure_debug.bal");
        BIREmitter birEmitter = BIREmitter.getInstance(new CompilerContext());
        var result = BCompileUtil.generateBIR("test-src/closures/closure_debug.bal");
        Object returns = BRunUtil.invoke(compileResult, "t1");
        Assert.assertEquals(returns, 2L);
    }
}
