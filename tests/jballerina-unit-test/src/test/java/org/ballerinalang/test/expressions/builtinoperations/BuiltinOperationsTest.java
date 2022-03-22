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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        Object arr = BRunUtil.invoke(result, "testIsNaN", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test mathematical operation that returns Infinity")
    public void testIsInfinity() {
        Object arr = BRunUtil.invoke(result, "testIsInfinite", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test mathematical operation that returns a finite number")
    public void testIsFinite() {
        Object arr = BRunUtil.invoke(result, "testIsFinite", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test result that is returned from a mathematical operation")
    public void testWithCalc() {
        Object arr = BRunUtil.invoke(result, "testWithCalc", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test result that is returned from a mod with divisor as zero")
    public void testModWithDivisorAsZero() {
        Object arr = BRunUtil.invoke(result, "testModWithDivisorAsZero", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test result that is returned from a mod with both dividend and divisor as zero")
    public void testModZeroByZero() {
        Object arr = BRunUtil.invoke(result, "testModZeroByZero", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test result that is returned from a mod with divisor as a finite number")
    public void testModWithDivisorAsFinite() {
        Object arr = BRunUtil.invoke(result, "testModWithDivisorAsFinite", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertFalse((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertTrue((Boolean) returns.get(2));
    }

    @Test(description = "Test negative tests")
    public void testNegativeTests() {
        Assert.assertEquals(resNegative.getErrorCount(), 5);
        BAssertUtil.validateError(resNegative, 0, "undefined function 'isNaN' in type 'int'", 20, 28);
        BAssertUtil.validateError(resNegative, 1, "undefined function 'isInfinite' in type 'int'", 21, 33);
        BAssertUtil.validateError(resNegative, 2, "undefined function 'isFinite' in type 'int'", 22, 28);
        BAssertUtil.validateError(resNegative, 3, "undefined function 'isNaN' in type 'int'", 24, 25);
        BAssertUtil.validateError(resNegative, 4, "undefined function 'isInfinite' in type 'int'", 25, 34);
    }
}
