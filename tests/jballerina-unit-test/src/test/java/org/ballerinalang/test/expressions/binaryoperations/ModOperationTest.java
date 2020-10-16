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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of modules operator.
 */
public class ModOperationTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/mod-operation.bal");
    }

    @Test(description = "Test two int mod expression")
    public void testIntMultiplyExpr() {
        intMod(1, 1, 0);
        intMod(10, 4, 2);
        intMod(4, 10, 4);
        intMod(-4, 10, -4);
    }

    @Test(description = "Test two float mod expression")
    public void testFloatDivideExpr() {
        floatMod(1, 1, 0);
        floatMod(10, 4, 2);
        floatMod(4, 10, 4);
        floatMod(-4, 10, -4);
    }

    @Test(description = "Test int float mod expression")
    public void testIntFloatModeExpr() {
        intFloatMod(1, 1.5f, 1);
        intFloatMod(10, 3.5f, 3);
        intFloatMod(4, 10.5f, 4);
        intFloatMod(-4, 10.5f, -4);
    }

    @Test(description = "Test float int mod expression")
    public void testFloatIntModeExpr() {
        floatIntMod(1.5f, 1, 0.5);
        floatIntMod(10.5f, 3, 1.5);
        floatIntMod(4.5f, 10, 4.5);
        floatIntMod(-4.5f, 10, -4.5);
    }

    private void intMod(int val1, int val2, long expected) {
        BValue[] args = { new BInteger(val1), new BInteger(val2) };
        BValue[] returns = BRunUtil.invoke(result, "intMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    private void floatMod(float val1, float val2, double expected) {
        BValue[] args = { new BFloat(val1), new BFloat(val2) };
        BValue[] returns = BRunUtil.invoke(result, "floatMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    private void intFloatMod(int val1, float val2, double expected) {
        BValue[] args = { new BInteger(val1), new BFloat(val2) };
        BValue[] returns = BRunUtil.invoke(result, "intFloatMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    private void floatIntMod(float val1, int val2, double expected) {
        BValue[] args = { new BFloat(val1), new BInteger(val2) };
        BValue[] returns = BRunUtil.invoke(result, "floatIntMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}DivisionByZero \\{\"message\":\" / " +
                    "by zero\"\\}.*")
    public void testIntModZero() {
        BRunUtil.invoke(result, "intMod", new BValue[]{new BInteger(2000), new BInteger(0)});
    }

    @Test
    public void testFloatModZero() {
        BValue[] returns = BRunUtil.invoke(result, "floatMod", new BValue[]{new BFloat(200.1), new BFloat(0.0)});
        Assert.assertEquals(returns[0].stringValue(), "NaN");
    }

    @Test
    public void testFloatModIntZero() {
        BValue[] returns = BRunUtil.invoke(result, "floatIntMod", new BValue[]{new BFloat(200.1), new BInteger(0)});
        Assert.assertEquals(returns[0].stringValue(), "NaN");
    }

    @Test
    public void testIntModFloatZero() {
        BValue[] returns = BRunUtil.invoke(result, "intFloatMod", new BValue[]{new BInteger(2100), new BFloat(0.0)});
        Assert.assertEquals(returns[0].stringValue(), "NaN");
    }
}
