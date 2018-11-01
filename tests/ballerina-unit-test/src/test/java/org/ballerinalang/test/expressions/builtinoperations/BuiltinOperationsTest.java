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
package org.ballerinalang.test.expressions.builtinoperations;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of the builtin operations.
 * @version 0.983.0
 */
public class BuiltinOperationsTest {

    private CompileResult result;
    private CompileResult resNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/builtinoperations.bal");
        resNegative = BCompileUtil.compile("test-src/expressions/builtinoperations/builtinoperations-negative.bal");
    }

    @Test(description = "Test mathematical operation that returns NaN")
    public void testIsNaN() {
        BValue[] returns = BRunUtil.invoke(result, "testIsNaN", new BValue[0]);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test(description = "Test mathematical operation that returns Infinity")
    public void testIsInfinity() {
        BValue[] returns = BRunUtil.invoke(result, "testIsInfinite", new BValue[0]);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test(description = "Test mathematical operation that returns a finite number")
    public void testIsFinite() {
        BValue[] returns = BRunUtil.invoke(result, "testIsFinite", new BValue[0]);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test(description = "Test result that is returned from a mathematical operation")
    public void testWithCalc() {
        BValue[] returns = BRunUtil.invoke(result, "testWithCalc", new BValue[0]);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test(description = "Test negative tests")
    public void testNegativeTests() {
        Assert.assertEquals(resNegative.getErrorCount(), 5);
        BAssertUtil.validateError(resNegative, 0, "undefined function 'int.isNaN'", 20, 21);
        BAssertUtil.validateError(resNegative, 1, "undefined function 'int.isInfinite'", 21, 21);
        BAssertUtil.validateError(resNegative, 2, "undefined function 'int.isFinite'", 22, 21);
        BAssertUtil.validateError(resNegative, 3, "undefined function 'int.isNaN'", 24, 18);
        BAssertUtil.validateError(resNegative, 4, "undefined function 'int.isInfinite'", 25, 22);
    }
}
