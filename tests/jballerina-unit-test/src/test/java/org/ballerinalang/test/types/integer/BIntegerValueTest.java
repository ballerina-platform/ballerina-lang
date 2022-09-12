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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of int values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 * <p>
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
        Object returns = BRunUtil.invoke(result, "testIntegerValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 10, "Invalid int value returned.");
    }

    @Test(description = "Test negative long value assignment")
    public void testNegativeIntegerValue() {
        Object returns = BRunUtil.invoke(result, "testNegativeIntegerValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, (-10), "Invalid int value returned.");
    }

    @Test(description = "Test long(hex) value assignment")
    public void testHexValue() {
        Object returns = BRunUtil.invoke(result, "testHexValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 10, "Invalid int value returned.");
    }

    @Test(description = "Test negative long(hex) value assignment")
    public void testNegativeHexValue() {
        Object returns = BRunUtil.invoke(result, "testNegativeHaxValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, (-10), "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment from a value returned by function")
    public void testIntegerValueAssignmentByReturnValue() {
        Object returns = BRunUtil.invoke(result, "testIntegerValueAssignmentByReturnValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 10, "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerParameter() {
        Object[] args = {(20)};
        Object returns = BRunUtil.invoke(result, "testIntegerParameter", args);

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 20, "Invalid int value returned.");
    }

    @Test(description = "Test long value Addition")
    public void testIntegerValueAddition() {
        Object returns = BRunUtil.invoke(result, "testIntegerAddition", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 19, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Addition")
    public void testIntegerTypesValueAddition() {
        Object returns = BRunUtil.invoke(result, "testIntegerTypesAddition", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 20, "Invalid int value returned.");
    }

    @Test(description = "Test long value Subtraction")
    public void testIntegerValueSubtraction() {
        Object returns = BRunUtil.invoke(result, "testIntegerSubtraction", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 10, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Subtraction")
    public void testIntegerTypesValueSubtraction() {
        Object returns = BRunUtil.invoke(result, "testIntegerTypesSubtraction", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, -10, "Invalid int value returned.");
    }

    @Test(description = "Test long value Multiplication")
    public void testIntegerValueMultiplication() {
        Object returns = BRunUtil.invoke(result, "testIntegerMultiplication", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 10, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Multiplication")
    public void testIntegerTypesValueMultiplication() {
        Object returns = BRunUtil.invoke(result, "testIntegerTypesMultiplication", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 1, "Invalid int value returned.");
    }

    @Test(description = "Test long value Division")
    public void testIntegerValueDivision() {
        Object returns = BRunUtil.invoke(result, "testIntegerDivision", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 5, "Invalid int value returned.");
    }

    @Test(description = "Test integer types value Division")
    public void testIntegerTypesValueDivision() {
        Object returns = BRunUtil.invoke(result, "testIntegerTypesDivision", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, 1, "Invalid int value returned.");
    }

    @Test(description = "Test record fields with int:subtypes")
    public void testIntSubtypeField() {
        BRunUtil.invoke(result, "testIntSubtypeField");
    }

    @Test(description = "Test static type of unary expression when static type of expression is int:subtype")
    public void testStaticTypeOfUnaryExpr() {
        BRunUtil.invoke(result, "testStaticTypeOfUnaryExpr");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
