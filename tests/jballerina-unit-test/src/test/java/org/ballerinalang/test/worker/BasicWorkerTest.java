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
package org.ballerinalang.test.worker;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Basic worker related tests.
 */
public class BasicWorkerTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/basic-worker-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }
    
    @Test
    public void workerDeclTest() {
        BRunUtil.invoke(result, "workerDeclTest", new BValue[0]);
    }
    
    @Test
    public void simpleWorkerMessagePassingTest() {
        BRunUtil.invoke(result, "simpleWorkerMessagePassingTest", new BValue[0]);
    }
    
    @Test
    public void basicForkTest() {
        BValue[] vals = BRunUtil.invoke(result, "basicForkTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 10);
    }
    
    @Test
    public void forkWithMessageParsingTest() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithMessageParsingTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 5);
    }
    
    @Test
    public void forkWithWaitForAny() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitForAny", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger val = (BInteger) vals[0];
        Assert.assertTrue(val.intValue() ==  15 || val.intValue() == 5);
    }
    
    @Test
    public void workerReturnTest() {
        BValue[] vals = BRunUtil.invoke(result, "workerReturnTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger ret = (BInteger) vals[0];
        Assert.assertEquals(ret.intValue(), 51);
    }

    @Test
    public void workerSameThreadSchedulingTest() {
        BValue[] vals = BRunUtil.invoke(result, "workerSameThreadTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BMap result = (BMap) vals[0];
        Assert.assertEquals(result.get("w"), result.get("w1"));
        Assert.assertEquals(result.get("w"), result.get("w2"));
    }
}
