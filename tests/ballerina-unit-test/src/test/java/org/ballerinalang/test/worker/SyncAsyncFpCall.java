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
package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test related to `start`.
 */
public class SyncAsyncFpCall {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/sync-and-async-fp-call.bal");
        Assert.assertEquals(result.getErrorCount(), 0, result.toString());
    }

    @Test
    public void syncAsyncFpCallTest() {
        BValue[] returns = BRunUtil.invoke(result, "syncAsyncFpCall", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BBoolean ret = (BBoolean) returns[0];
        Assert.assertTrue(ret.booleanValue(), "Increment ran sequentially.");
    }

}
