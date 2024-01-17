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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(compileResult, "basicWorkerTest", new Object[0]);
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test variable mutability with tuples")
    public void testWithTuples() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testWithTuples", new Object[0]);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0).toString().contains("Changed inside worker 1!!!"));
        Assert.assertTrue(returns.get(0).toString().contains("Changed inside worker 2!!!"));
        Assert.assertEquals(returns.get(1), 150L);
    }

    @Test(description = "Test variable mutability with maps")
    public void testWithMaps() {
        Object returns = BRunUtil.invoke(compileResult, "testWithMaps");
        BMap resMap = (BMap) returns;
        Assert.assertEquals(resMap.size(), 7);
        Assert.assertEquals(resMap.get(StringUtils.fromString("a")).toString(), "AAAA");
        Assert.assertEquals(resMap.get(StringUtils.fromString("b")).toString(), "B");
        Assert.assertEquals(resMap.get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals(resMap.get(StringUtils.fromString("d")).toString(), "D");
        Assert.assertEquals(resMap.get(StringUtils.fromString("e")).toString(), "EEE");
        Assert.assertEquals(resMap.get(StringUtils.fromString("x")).toString(), "X");
        Assert.assertEquals(resMap.get(StringUtils.fromString("n")).toString(), "N");
    }

    @Test(description = "Test variable mutability with complex workers")
    public void complexWorkerTest() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "complexWorkerTest");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0), 400L);

        BMap resMap = (BMap) returns.get(1);
        Assert.assertEquals(resMap.size(), 6);
        Assert.assertEquals(resMap.get(StringUtils.fromString("a")).toString(), "AAAA");
        Assert.assertEquals(resMap.get(StringUtils.fromString("b")).toString(), "BBBB");
        Assert.assertEquals(resMap.get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals(resMap.get(StringUtils.fromString("d")).toString(), "D");
        Assert.assertEquals(resMap.get(StringUtils.fromString("e")).toString(), "EE");
        Assert.assertEquals(resMap.get(StringUtils.fromString("m")).toString(), "MMM");
    }

    @Test(description = "Test variable mutability with records")
    public void testWithRecords() {
        Object returns = BRunUtil.invoke(compileResult, "testWithRecords");
        Assert.assertEquals(((BMap) returns).size(), 3);
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"Adam Page\",\"age\":24,\"email\":\"adamp@wso2.com\"}");
    }

    @Test(description = "Test variable mutability with objects")
    public void testWithObjects() {
        Object returns = BRunUtil.invoke(compileResult, "testWithObjects");
        Assert.assertEquals(returns.toString(), "{age:40, name:Adam, fullName:Adam Adam Page}");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
