/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.javainterop.basic;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for java interop async invocations.
 *
 * @since 1.0.0
 */
public class AsyncTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/async_test.bal");
    }

    @Test
    public void testAcceptNothing() {
        Object returns = BRunUtil.invoke(result, "asyncTest");

        Assert.assertEquals(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 42L);
    }

    @Test
    public void testAsyncComplete() {
        CompileResult compileResult =
                BCompileUtil.compileWithoutInitInvocation("test-src/javainterop/basic/async_test.bal");
        BRunUtil.ExitDetails output = BRunUtil.run(compileResult, new String[]{});
        Assert.assertTrue(output.errorOutput.contains("cannot complete the same future twice."));
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
