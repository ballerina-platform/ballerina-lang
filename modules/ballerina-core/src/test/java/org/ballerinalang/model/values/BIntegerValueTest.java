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
package org.ballerinalang.model.values;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
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
    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/values/integer-value.bal");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test negative long value assignment")
    public void testNegativeIntegerValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testNegativeIntegerValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), (-10), "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment from a value returned by function")
    public void testIntegerValueAssignmentByReturnValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerValueAssignmentByReturnValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerParameter() {
        BValue[] args = {new BInteger(20)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 20, "Invalid int value returned.");
    }

    @Test(description = "Test long value Addition")
    public void testIntegerValueAddition() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 19, "Invalid int value returned.");
    }

    @Test(description = "Test long value Subtraction")
    public void testIntegerValueSubtraction() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test long value Multiplication")
    public void testIntegerValueMultiplication() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 10, "Invalid int value returned.");
    }

    @Test(description = "Test long value Division")
    public void testIntegerValueDivision() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntegerDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), 5, "Invalid int value returned.");
    }
}
