/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.object;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for object with attached functions invocations as function pointers in ballerina.
 */
public class ObjectAttachedFunctionPointerTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/object-attached-function-pointers.bal");
    }

    @Test(description = "Test basic function pointer test")
    public void testBasicAttachedFunctionPointerTest() {
        Object returns = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(returns, 60L);
    }

    @Test(description = "Test object attached function pointer with lambda")
    public void testAttachedFunctionPointerWithLambdaTest() {
        Object returns = BRunUtil.invoke(compileResult, "test2");
        Assert.assertEquals(returns, 60L);
    }

    @Test(description = "Test basic externally attached function pointer")
    public void testBasicExternalAttachedFunctionPointerTest() {
        Object returns = BRunUtil.invoke(compileResult, "test3");
        Assert.assertEquals(returns, 53L);
    }

    @Test(description = "Test externally attached function pointer with lambda")
    public void testExternalAttachedFunctionPointerWithLambdaTest() {
        Object returns = BRunUtil.invoke(compileResult, "test4");
        Assert.assertEquals(returns, 60L);
    }

    @Test(description = "Test multi level lambda with attached function")
    public void testMultiLevelLambdaWithinAttachedFunction() {
        Object returns = BRunUtil.invoke(compileResult, "test5");
        Assert.assertEquals(returns, 65L);
    }

    @Test(description = "Test invoke attached function as function pointer1")
    public void testInvokeAttachedFunctionAsFunctionPointer1() {
        Object returns = BRunUtil.invoke(compileResult, "test6");
        Assert.assertEquals(returns, 106L);
    }

    @Test(description = "Test invoke attached function as function pointer2")
    public void testInvokeAttachedFunctionAsFunctionPointer2() {
        Object returns = BRunUtil.invoke(compileResult, "test7");
        Assert.assertEquals(returns, 106L);
    }

    @Test(description = "Test invoke attached function as function pointer3")
    public void testInvokeAttachedFunctionAsFunctionPointer3() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "test8");
        Assert.assertEquals(returns.get(0).toString(), "A");
        Assert.assertEquals(returns.get(1), 2L);
    }

    @Test(description = "Test invoke attached function as function pointer4")
    public void testInvokeAttachedFunctionAsFunctionPointer4() {
        Object returns = BRunUtil.invoke(compileResult, "test9");
        Assert.assertEquals(returns.toString(), "finally");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
