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
 * This test class will test the behaviour of double values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 * <p>
 * Defining a double value
 * double b;
 * b = 10.1d;
 */
public class BFloatValueTest {
    private static final double DELTA = 0.01;
    private ProgramFile bLangProgram;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/values/float-value.bal");
    }

    @Test(description = "Test double value assignment")
    public void testFloatValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.1f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test negative double value assignment")
    public void testNegativeFloatValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testNegativeFloatValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), (-10.1f), DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment from a value returned by function")
    public void testFloatValueAssignmentByReturnValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatValueAssignmentByReturnValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.1d, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment")
    public void testFloatParameter() {
        BValue[] args = {new BFloat(3.3f)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 3.3f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value Addition")
    public void testFloatValueAddition() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 20.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Subtraction")
    public void testFloatValueSubtraction() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Multiplication")
    public void testFloatValueMultiplication() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 13.75d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Division")
    public void testFloatValueDivision() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testFloatDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 5.0d, "Invalid float value returned.");
    }
}
