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
package org.ballerinalang.test.expressions.async;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Basic aync operations tests.
 */
public class BasicAsyncOperationsTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/expressions/async/basic-async-operations.bal");
        Assert.assertEquals(this.result.getErrorCount(), 0);
    }
    
    @Test
    public void testAsyncNonNativeBasic1() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic1", new Object[0]);
        Assert.assertEquals(returns, 7L);
    }
    
    @Test
    public void testAsyncNonNativeBasic2() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic2", new Object[0]);
        Assert.assertEquals(returns, 19L);
    }
    
    @Test
    public void testAsyncNonNativeBasic3() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic3", new Object[0]);
        Assert.assertEquals(returns, 31L);
    }
    
    @Test
    public void testAsyncNonNativeBasic4() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic4", new Object[0]);
        Assert.assertEquals(returns, 3L);
    }
    
    @Test
    public void testAsyncNonNativeBasic5() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic5", new Object[0]);
        Assert.assertEquals(returns, 31.0);
    }
    
    @Test
    public void testAsyncNonNativeBasic6() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic6", new Object[0]);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.future}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic7() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic7", new Object[0]);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.future}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic8() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic8", new Object[0]);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAsyncNonNativeBasic9() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic9", new Object[0]);
        Assert.assertEquals(returns, 7L);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.future}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic10() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic10", new Object[0]);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.future}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic11() {
        Object returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic11", new Object[0]);
        Assert.assertTrue((Boolean) returns);
    }
    
    @Test
    public void testAsyncObjectAttachedFunctions() {
        Object returns = BRunUtil.invoke(result, "testAsyncObjectAttachedFunctions");
        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void testAsyncInvWithoutDefaultParams() {
        Object returns = BRunUtil.invoke(result, "testAsyncInvWithoutDefaultParams");
        Assert.assertEquals(returns, 5L);
    }

    @Test
    public void testAsyncInvWithDefaultParams() {
        Object returns = BRunUtil.invoke(result, "testAsyncInvWithDefaultParams");
        Assert.assertEquals(returns, 45L);
    }

    @Test
    public void testAttachedAsyncInvWithoutDefaultParams() {
        Object returns = BRunUtil.invoke(result, "testAttachedAsyncInvWithoutDefaultParams");
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testAttachedAsyncInvWithDefaultParams() {
        Object returns = BRunUtil.invoke(result, "testAttachedAsyncInvWithDefaultParams");
        Assert.assertEquals(returns, 9L);
    }

    @Test (description = "Test negative issues with future")
    public void testAsyncNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/expressions/async/async-operations-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'future<int>', found 'int'", 3, 23);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'future<int>', found 'int'", 9, 23);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'future<int>', found 'any'", 15, 23);
    }
}
