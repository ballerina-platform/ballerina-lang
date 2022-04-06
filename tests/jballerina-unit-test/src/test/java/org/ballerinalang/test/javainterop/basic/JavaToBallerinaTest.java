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
import org.testng.annotations.Test;

/**
 * Test cases for interop java originated invocations.
 *
 * @since 1.0.0
 */
public class JavaToBallerinaTest {

    @Test
    public void testUsingExistingBallerinaRuntime() {
        CompileResult result = BCompileUtil.compile("test-src/javainterop/basic/java_to_bal_test.bal");
        Object returns = BRunUtil.invoke(result, "timerTest");

        Assert.assertEquals(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 3L);
    }

    @Test
    public void testReturnValue() {
        CompileResult result = BCompileUtil.compile("test-src/javainterop/basic/java_to_bal_with_return.bal");
        Object returns = BRunUtil.invoke(result, "returnValueTest");

        Assert.assertEquals(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 50L);
    }


}
