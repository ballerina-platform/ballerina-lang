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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic1", new BValue[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }
    
    @Test
    public void testAsyncNonNativeBasic2() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic2", new BValue[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 19);
    }
    
    @Test
    public void testAsyncNonNativeBasic3() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic3", new BValue[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 31);
    }
    
    @Test
    public void testAsyncNonNativeBasic4() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic4", new BValue[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }
    
    @Test
    public void testAsyncNonNativeBasic5() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic5", new BValue[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 31.0);
    }
    
    @Test
    public void testAsyncNonNativeBasic6() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic6", new BValue[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*{error}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic7() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic7", new BValue[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*{error}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic8() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic8", new BValue[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAsyncNonNativeBasic9() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic9", new BValue[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*{error}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic10() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic10", new BValue[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*{error}FutureAlreadyCancelled.*")
    public void testAsyncNonNativeBasic11() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncNonNativeBasic11", new BValue[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
    
    @Test
    public void testAsyncObjectAttachedFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncObjectAttachedFunctions");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testAsyncInvWithoutDefaultParams() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncInvWithoutDefaultParams");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testAsyncInvWithDefaultParams() {
        BValue[] returns = BRunUtil.invoke(result, "testAsyncInvWithDefaultParams");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 45);
    }

    @Test
    public void testAttachedAsyncInvWithoutDefaultParams() {
        BValue[] returns = BRunUtil.invoke(result, "testAttachedAsyncInvWithoutDefaultParams");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testAttachedAsyncInvWithDefaultParams() {
        BValue[] returns = BRunUtil.invoke(result, "testAttachedAsyncInvWithDefaultParams");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);
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
