/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.runtime;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test processing invocation context.
 */
public class InvocationContextTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/invocation-context.bal");
    }

    @Test(enabled = false, description = "Test case for accessing invocationId from invocation context")
    public void testInvocationContextId() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInvocationContextId");
        Assert.assertTrue(returns[0] instanceof BString);
    }

    @Test(enabled = false, description = "Test case for accessing attributes from invocation context")
    public void testInvocationContextAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInvocationContextAttributes");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].size(), 0);
    }
}
