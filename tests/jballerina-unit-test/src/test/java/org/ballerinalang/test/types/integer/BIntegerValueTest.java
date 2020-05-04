/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.integer;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of int values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 *
 * Defining a Integer value
 * long b;
 * b = 10.1L;
 */
public class BIntegerValueTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/integer/integer-value.bal");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test negative long value assignment")
    public void testNegativeIntegerValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeIntegerValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), (-10), "Invalid int value returned.");
    }

    @Test(description = "Test long(hex) value assignment")
    public void testHexValue() {
        BValue[] returns = BRunUtil.invoke(result, "testHexValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test negative long(hex) value assignment")
    public void testNegativeHexValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeHaxValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), (-10), "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment from a value returned by function")
    public void testIntegerValueAssignmentByReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerValueAssignmentByReturnValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerParameter() {
        BValue[] args = {new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, "testIntegerParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 20, "Invalid int value returned.");
    }

    @Test(description = "Test long value Addition")
    public void testIntegerValueAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 19, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Addition")
    public void testIntegerTypesValueAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerTypesAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 20, "Invalid int value returned.");
    }

    @Test(description = "Test long value Subtraction")
    public void testIntegerValueSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerSubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Subtraction")
    public void testIntegerTypesValueSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerTypesSubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), -10, "Invalid int value returned.");
    }

    @Test(description = "Test long value Multiplication")
    public void testIntegerValueMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Multiplication")
    public void testIntegerTypesValueMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerTypesMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 1, "Invalid int value returned.");
    }

    @Test(description = "Test long value Division")
    public void testIntegerValueDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 5, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Division")
    public void testIntegerTypesValueDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testIntegerTypesDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 1, "Invalid int value returned.");
    }
}
