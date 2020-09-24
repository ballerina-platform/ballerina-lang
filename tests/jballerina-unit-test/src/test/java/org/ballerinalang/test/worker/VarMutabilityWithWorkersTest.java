/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Variable mutability with worker related tests.
 *
 * @since 0.995.0
 */
public class VarMutabilityWithWorkersTest {

    private CompileResult compileResult;
    
    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/workers/var-mutability-with-workers.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0,
                            Arrays.asList(compileResult.getDiagnostics()).toString());
    }

    @Test(description = "Test variable mutability with basic types")
    public void basicWorkerTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "basicWorkerTest", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test(description = "Test variable mutability with tuples")
    public void testWithTuples() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithTuples", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0].stringValue().contains("Changed inside worker 1!!!"));
        Assert.assertTrue(returns[0].stringValue().contains("Changed inside worker 2!!!"));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 150);
    }

    @Test(description = "Test variable mutability with maps")
    public void testWithMaps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithMaps");
        BMap resMap = (BMap) returns[0];
        Assert.assertEquals(resMap.size(), 7);
        Assert.assertEquals(resMap.getMap().get("a").toString(), "AAAA");
        Assert.assertEquals(resMap.getMap().get("b").toString(), "B");
        Assert.assertEquals(resMap.getMap().get("c").toString(), "C");
        Assert.assertEquals(resMap.getMap().get("d").toString(), "D");
        Assert.assertEquals(resMap.getMap().get("e").toString(), "EEE");
        Assert.assertEquals(resMap.getMap().get("x").toString(), "X");
        Assert.assertEquals(resMap.getMap().get("n").toString(), "N");
    }

    @Test(description = "Test variable mutability with complex workers")
    public void complexWorkerTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "complexWorkerTest");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 400);

        BMap resMap = (BMap) returns[1];
        Assert.assertEquals(resMap.size(), 6);
        Assert.assertEquals(resMap.getMap().get("a").toString(), "AAAA");
        Assert.assertEquals(resMap.getMap().get("b").toString(), "BBBB");
        Assert.assertEquals(resMap.getMap().get("c").toString(), "C");
        Assert.assertEquals(resMap.getMap().get("d").toString(), "D");
        Assert.assertEquals(resMap.getMap().get("e").toString(), "EE");
        Assert.assertEquals(resMap.getMap().get("m").toString(), "MMM");
    }

    @Test(description = "Test variable mutability with records")
    public void testWithRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithRecords");
        Assert.assertEquals(((BMap) returns[0]).size(), 3);
        Assert.assertEquals(((BMap) returns[0]).getMap().toString(), "{name=Adam Page, age=24, email=adamp@wso2.com}");
    }

    @Test(description = "Test variable mutability with objects")
    public void testWithObjects() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithObjects");
        Assert.assertEquals(((BMap) returns[0]).size(), 3);
        Assert.assertEquals(((BMap) returns[0]).getMap().toString(), "{age=40, name=Adam, fullName=Adam Adam Page}");
    }
}
