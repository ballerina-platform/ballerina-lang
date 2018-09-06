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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
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

    @Test (description = "Test negative issues with future")
    public void testAsyncNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/expressions/async/async-operations-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'future', found 'int'", 3, 18);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'future', found 'int'", 9, 18);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'future', found 'any'", 15, 18);
    }
}
