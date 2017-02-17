/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Primitive mod expression test.
 */
public class ModExpressionTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/mod-expr.bal");
    }

    @Test(description = "Test two int mod expression")
    public void testIntMultiplyExpr() {
        intMod(1, 1, 0);
        intMod(10, 4, 2);
        intMod(4, 10, 4);
        intMod(-4, 10, -4);
    }

    @Test(description = "Test two long mod expression")
    public void testLongDivideExpr() {
        longMod(1, 1, 0);
        longMod(10, 4, 2);
        longMod(4, 10, 4);
        longMod(-4, 10, -4);
    }

    @Test(description = "Test two float mod expression")
    public void testFloatDivideExpr() {
        floatMod(1, 1, 0);
        floatMod(10, 4, 2);
        floatMod(4, 10, 4);
        floatMod(-4, 10, -4);
    }

    @Test(description = "Test two double mod expression")
    public void testDoubleDivideExpr() {
        doubleMod(1, 1, 0);
        doubleMod(10, 4, 2);
        doubleMod(4, 10, 4);
        doubleMod(-4, 10, -4);
    }

    private void intMod(int val1, int val2, int expected) {
        BValue[] args = { new BInteger(val1), new BInteger(val2) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "intMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    private void longMod(long val1, long val2, long expected) {
        BValue[] args = { new BLong(val1), new BLong(val2) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "longMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        Assert.assertEquals(actual, expected);
    }

    private void floatMod(float val1, float val2, float expected) {
        BValue[] args = { new BFloat(val1), new BFloat(val2) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "floatMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    private void doubleMod(double val1, double val2, double expected) {
        BValue[] args = { new BDouble(val1), new BDouble(val2) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "doubleMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actual, expected);
    }
}
