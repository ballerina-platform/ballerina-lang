/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.invocations;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.BRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * package init function invocation test.
 *
 * @since 0.8.0
 */
public class PackageInitInvocationTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/packageinit");
    }

    @Test
    public void invokeFunctionWithParams() {
        Object values = BRunUtil.invoke(result, "testInitInvocation");
        Assert.assertTrue(values instanceof Long);
        Assert.assertEquals((values), 19L);
    }

    @Test
    public void testGetValueFromPackageLevelAsyncInvocation() {
        Object values = BRunUtil.invoke(result, "testGetIntValue");
        Assert.assertTrue(values instanceof Long);
        Assert.assertEquals(values, 899L);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
