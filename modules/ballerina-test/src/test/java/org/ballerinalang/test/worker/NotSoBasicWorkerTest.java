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
    
//    function forkJoinWithMessagePassingTimeoutNotTriggered() (map) {
//        map m = {};
//        fork {
//           worker w1 {
//             int a = 5;
//             a -> w2;
//             int b = 0;
//             b <- w2;
//             b -> fork;
//           }
//           worker w2 {
//             int a = 0;
//             a <- w1;
//             int b = 15;
//             b -> w1;
//             a -> fork;
//           }
//        } join (all) (map results) {
//            any[] anyArray;
//            int b;
//            anyArray, _ = (any[]) m["w1"];
//            b, _ = (int) anyArray[0];
//            int a;
//            anyArray, _ = (any[]) m["w2"];
//            a, _ = (int) anyArray[0];
//            m["x"] = (a + 1) * b;
//        } timeout (5) (map results) { 
//            m["x"] = 15; 
//        }
//        return m;
//    }
    
    @Test (enabled = false)
    public void forkJoinWithMessagePassingTimeoutNotTriggered() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithMessagePassingTimeoutNotTriggered", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 90);
    }
    
    @Test
    public void chainedWorkerSendReceive() {
        BValue[] vals = BTestUtils.invoke(result, "chainedWorkerSendReceive", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 12);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin1() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithSomeSelectedJoin1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 15);
        Assert.assertEquals(map.get("y").intValue(), 5);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin2() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithSomeSelectedJoin2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 320);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin3() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithSomeSelectedJoin3", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 160);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin4() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithSomeSelectedJoin4", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 10);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin5() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithSomeSelectedJoin5", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 555);
    }
    
    @Test
    public void forkJoinWithAllSelectedJoin1() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithAllSelectedJoin1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 33);
    }
    
    @Test
    public void forkJoinWithAllSelectedJoin2() {
        BValue[] vals = BTestUtils.invoke(result, "forkJoinWithAllSelectedJoin2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 777);
    }
    
}
