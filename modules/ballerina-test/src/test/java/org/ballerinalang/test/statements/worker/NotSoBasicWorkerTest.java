/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.worker;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Advanced worker related tests.
 */
public class NotSoBasicWorkerTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BTestUtils.compile("test-src/workers/not-so-basic-worker-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
    
    @Test
    public void forkJoinWithTimeoutTest1() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithTimeoutTest1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 15);
    }
    
    @Test
    public void forkJoinWithTimeoutTest2() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithTimeoutTest2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 25);
    }
    
}
